from datetime import datetime

from flask import Flask, jsonify, request
from flask_socketio import SocketIO, emit
import os
import time
import threading
import numpy as np
import re
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import StandardScaler
from deepstudy import process_logs_and_predict
from moxingpanduan import evaluate_model
import tensorflow as tf
from tensorflow.keras.models import load_model
import pickle
from kafka_producer import deepseek_producer as kafka

app = Flask(__name__)
socketio = SocketIO(app)


@app.route('/process_logs', methods=['GET'])
def process_logs():
    log_file_path = 'auth.log'

    if not os.path.exists(log_file_path):
        return jsonify({"error": f"Log file {log_file_path} does not exist"}), 404

    result = process_logs_and_predict(log_file_path)
    return jsonify(result)


@app.route('/evaluate', methods=['POST'])
def evaluate():
    data = request.json
    labeled_log_file = data.get('labeled_log_file')
    threshold = data.get('threshold', 0.8614609496866266)

    if not labeled_log_file or not os.path.exists(labeled_log_file):
        return jsonify({"error": "日志文件不存在"}), 400

    try:
        threshold = float(threshold)
    except ValueError:
        return jsonify({"error": "阈值必须是浮点数"}), 400

    try:
        results = evaluate_model(labeled_log_file, threshold=threshold)
    except Exception as e:
        return jsonify({"error": str(e)}), 500

    return jsonify(results)


def read_log_file(log_file_path):
    with open(log_file_path, 'r') as file:
        file.seek(0, 2)  # 跳到文件末尾
        last_position = file.tell()  # 记录当前文件位置
        while True:
            line = file.readline()
            if not line:
                time.sleep(1)  # 没有新日志，等待1秒
                continue
            yield line.strip()


# 加载模型和预处理工具


def predict_new_logs(log_file_path):
    model_file = 'conv_autoencoder_model.h5'
    vectorizer_file = 'vectorizer.pkl'
    scaler_file = 'scaler.pkl'
    threshold = 0.8614609496866266  # 阈值
    if os.path.exists(model_file):
        print("加载模型和预处理工具...")
        autoencoder = load_model(model_file)

        with open(vectorizer_file, 'rb') as f:
            vectorizer = pickle.load(f)

        with open(scaler_file, 'rb') as f:
            scaler = pickle.load(f)
    log_texts = []
    for log_text in read_log_file(log_file_path):
        log_texts.append(log_text)
        if len(log_texts) >= 1:
            current_batch = log_texts[:1]
            log_texts = log_texts[1:]

            # 文本向量化
            log_tfidf = vectorizer.transform(current_batch).toarray()
            print("TF-IDF特征维度：", log_tfidf.shape)

            # 提取时间戳、时间间隔、小时、分钟、登录类型和IP地址
            def extract_features(log_text):
                print(log_text)
                timestamp = re.search(r'(\w{3} \d{2} \d{2}:\d{2}:\d{2})', log_text)
                login_type = 1 if 'Accepted' in log_text else 0
                ip_address = re.search(r'from (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})', log_text)
                invalid_user = 1 if 'Invalid user' in log_text else 0
                auth_failure = 1 if re.search(r'authentication failure', log_text, re.IGNORECASE) else 0
                unknown = 1 if 'unknown' in log_text else 0
                user_unknown = 1 if 'user unknown' in log_text else 0

                if timestamp and ip_address:
                    timestamp = pd.to_datetime(timestamp.group(1), format='%b %d %H:%M:%S')
                    ip_address = int(''.join(ip_address.group(1).split('.')))
                    return [timestamp.hour, timestamp.minute, timestamp.second, timestamp.weekday(), login_type,
                            ip_address, invalid_user, auth_failure, unknown, user_unknown]

                return [0, 0, 0, 0, 0, login_type, 0, invalid_user, auth_failure, unknown, user_unknown]

            features = np.array([extract_features(log) for log in current_batch])
            print("额外特征维度：", features.shape)

            # 增加对短时间内多次失败尝试的检测
            def count_consecutive_failures(log_texts):
                failures = [1 if 'authentication failure' in log else 0 for log in log_texts]
                consecutive_failures = []
                count = 0
                for i in range(len(failures)):
                    if failures[i] == 1:
                        count += 1
                    else:
                        count = 0
                    consecutive_failures.append(count)
                return consecutive_failures

            consecutive_failures = count_consecutive_failures(current_batch)
            features = np.hstack((features, np.array(consecutive_failures).reshape(-1, 1)))
            print("增加连续失败尝试特征后的维度：", features.shape)

            # 确保 `combined_features` 形状一致
            combined_features = np.hstack((log_tfidf, features))
            print("合并特征维度：", combined_features.shape)

            # 检查训练时 StandardScaler 期望的特征数量
            expected_features = scaler.n_features_in_
            actual_features = combined_features.shape[1]

            if actual_features < expected_features:
                missing_cols = expected_features - actual_features
                zero_padding = np.zeros((combined_features.shape[0], missing_cols))
                combined_features = np.hstack((combined_features, zero_padding))
                print(f"补充 {missing_cols} 列，当前特征维度: {combined_features.shape[1]}")

            elif actual_features > expected_features:
                excess_cols = actual_features - expected_features
                combined_features = combined_features[:, :-excess_cols]
                print(f"删除 {excess_cols} 列，当前特征维度: {combined_features.shape[1]}")

            # 标准化特征
            combined_features = scaler.transform(combined_features)
            print("标准化后的特征维度：", combined_features.shape)

            # 使用模型进行预测
            predicted_log = autoencoder.predict(combined_features)

            # 计算每条日志的均方误差（MSE）
            mse = np.mean(np.square(combined_features - predicted_log), axis=1)
            print("MSE 值：", mse)

            # 计算这一组日志的平均MSE
            avg_mse = np.mean(mse)
            print(f"这一组日志的平均MSE：{avg_mse}")

            # 使用用户输入的阈值
            print(f"使用阈值: {threshold}")

            # 判断是否有攻击行为
            attack_detected = avg_mse > threshold
            if attack_detected:
                print("检测到攻击行为！")
            else:
                print("未检测到攻击行为。")

            # 发送预测结果
            data = {
                'avg_mse': avg_mse,
                'attack_detected': bool(attack_detected),
                'log_text': log_text,
            }
            kafka.send_result('deep_study_log', data)
        else:
            raise FileNotFoundError("模型文件或预处理工具文件未找到，请先训练模型。")


def start_real_time_predict():
    log_file_path = '/var/log/auth.log'
    if not os.path.exists(log_file_path):
        socketio.emit('error', {'message': f"Log file {log_file_path} does not exist"})
        return
    predict_new_logs(log_file_path)


if __name__ == '__main__':
    # 启动后台线程
    threading.Thread(target=start_real_time_predict).start()
    socketio.run(app, host="0.0.0.0", port=5000)