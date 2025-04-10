import pandas as pd
import numpy as np
import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import StandardScaler
import tensorflow as tf
from tensorflow.keras.models import load_model
import pickle
import os
from sklearn.metrics import accuracy_score, recall_score, precision_score, f1_score, confusion_matrix, roc_auc_score

def evaluate_model(labeled_log_file, model_file='conv_autoencoder_model.h5', vectorizer_file='vectorizer.pkl', scaler_file='scaler.pkl', threshold=None):
    # 加载模型和预处理工具
    if os.path.exists(model_file):
        print("加载模型和预处理工具...")
        autoencoder = load_model(model_file)
        
        with open(vectorizer_file, 'rb') as f:
            vectorizer = pickle.load(f)
        
        with open(scaler_file, 'rb') as f:
            scaler = pickle.load(f)
    else:
        raise FileNotFoundError("模型文件或预处理工具文件未找到，请先训练模型。")

    # 读取带标签的日志数据
    with open(labeled_log_file, 'r') as file:
        labeled_log_lines = file.readlines()

    # 将带标签的日志数据转换为DataFrame
    def parse_labeled_data(lines):
        labeled_data = pd.DataFrame(columns=['log_text', 'label'])
        group = []
        label = None
        for line in lines:
            if line.startswith('#'):
                label = int(line.strip().replace('#', ''))
                if group:  # 如果有日志组，添加到DataFrame
                    new_row = pd.DataFrame({'log_text': ['\n'.join(group)], 'label': [label]})
                    labeled_data = pd.concat([labeled_data, new_row], ignore_index=True)
                group = []  # 重置日志组
            else:
                group.append(line.strip())
        if group:  # 如果文件末尾还有未处理的日志
            new_row = pd.DataFrame({'log_text': ['\n'.join(group)], 'label': [label]})
            labeled_data = pd.concat([labeled_data, new_row], ignore_index=True)
        return labeled_data

    labeled_data = parse_labeled_data(labeled_log_lines)

    # 提取日志文本和标签
    log_texts = labeled_data['log_text'].tolist()
    labels = labeled_data['label'].astype(int).values  # 确保标签是整数

    # 文本向量化
    log_tfidf = vectorizer.transform(log_texts).toarray()
    print("TF-IDF特征维度：", log_tfidf.shape)

    # 提取时间戳、时间间隔、小时、分钟、登录类型和IP地址
    def extract_features(log_text):
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
            return [timestamp.hour, timestamp.minute, timestamp.second, timestamp.weekday(), login_type, ip_address, invalid_user, auth_failure, unknown, user_unknown]
        
        return [0, 0, 0, 0, login_type, 0, invalid_user, auth_failure, unknown, user_unknown]

    features = np.array([extract_features(log) for log in log_texts])
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

    consecutive_failures = count_consecutive_failures(log_texts)
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
        # 处理特征数量过多的情况，选择合适的处理方式
        excess_cols = actual_features - expected_features
        # 删除过多的特征
        combined_features = combined_features[:, :-excess_cols]
        print(f"删除 {excess_cols} 列，当前特征维度: {combined_features.shape[1]}")

    # 标准化特征
    combined_features = scaler.transform(combined_features)
    print("标准化后的特征维度：", combined_features.shape)

    # 使用模型进行预测
    predicted_log = autoencoder.predict(combined_features)

    # 计算每条日志的均方误差（MSE）
    mse = np.mean(np.square(combined_features - predicted_log), axis=1)

    # 使用用户输入的阈值
    if threshold is None:
        threshold = 0.8614609496866266  # 默认阈值
    else:
        print(f"使用用户输入的阈值: {threshold}")

    # 生成预测标签（0: 正常, 1: 异常）
    predicted_labels = (mse > threshold).astype(int)

    # 确保 `labels` 和 `predicted_labels` 维度匹配
    if len(labels) != len(predicted_labels):
        raise ValueError(f"标签数量不匹配！真实标签 {len(labels)}，预测标签 {len(predicted_labels)}")

    # 计算性能指标
    accuracy = accuracy_score(labels, predicted_labels)
    recall = recall_score(labels, predicted_labels)
    precision = precision_score(labels, predicted_labels)
    f1 = f1_score(labels, predicted_labels)
    roc_auc = roc_auc_score(labels, mse)  # 使用MSE作为概率预测
    conf_matrix = confusion_matrix(labels, predicted_labels)

    print(f"准确率（Accuracy）: {accuracy:.4f}")
    print(f"召回率（Recall）: {recall:.4f}")
    print(f"精确率（Precision）: {precision:.4f}")
    print(f"F1分数: {f1:.4f}")
    print(f"ROC-AUC分数: {roc_auc:.4f}")
    print(f"混淆矩阵:\n{conf_matrix}")

    return {
        "accuracy": accuracy,
        "recall": recall,
        "precision": precision,
        "f1_score": f1,
        "roc_auc": roc_auc,
        "confusion_matrix": conf_matrix.tolist(),
    }