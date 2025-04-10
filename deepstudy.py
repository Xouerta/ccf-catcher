import os
import pandas as pd
import numpy as np
import random
import re
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import StandardScaler
import tensorflow as tf
from tensorflow.keras.models import Model, load_model
from tensorflow.keras.layers import Input, Conv1D, MaxPooling1D, UpSampling1D, Flatten, Dense
import pickle
from datetime import datetime

def process_logs_and_predict(log_file_path, model_file='conv_autoencoder_model.h5', vectorizer_file='vectorizer.pkl', scaler_file='scaler.pkl'):
    # 读取日志数据
    with open(log_file_path, 'r') as file:
        log_lines = file.readlines()

    # 将日志数据转换为DataFrame
    data = pd.DataFrame(log_lines, columns=['log_text'])

    # 提取时间戳
    data['timestamp'] = data['log_text'].str.extract(r'(\w{3} \d{2} \d{2}:\d{2}:\d{2})')
    data['timestamp'] = pd.to_datetime(data['timestamp'], format='%b %d %H:%M:%S', errors='coerce')

    # 检查是否有空值
    if data['timestamp'].isnull().any():
        print("警告：存在格式不匹配的时间戳，已将其设置为NaT。")
        data.dropna(subset=['timestamp'], inplace=True)

    # 计算时间间隔（秒）
    data['time_diff'] = data['timestamp'].diff().dt.total_seconds().fillna(0)

    # 提取小时和分钟
    data['hour'] = data['timestamp'].dt.hour
    data['minute'] = data['timestamp'].dt.minute

    # 提取登录类型（成功或失败）
    data['login_type'] = data['log_text'].apply(lambda x: 1 if 'Accepted' in x else 0)

    # 提取 IP 地址
    def extract_ip(log_text):
        match = re.search(r'from (\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})', log_text)
        return match.group(1) if match else '0.0.0.0'

    data['ip_address'] = data['log_text'].apply(extract_ip)
    data['ip_address'] = data['ip_address'].apply(lambda x: int(''.join(x.split('.'))))

    # 文本向量化
    vectorizer = TfidfVectorizer(max_features=1000)
    log_tfidf = vectorizer.fit_transform(data['log_text']).toarray()

    # 将时间间隔、登录类型、小时、分钟、IP 地址和文本特征合并
    combined_features = np.hstack((log_tfidf, data[['time_diff', 'login_type', 'hour', 'minute', 'ip_address']].values))

    # 标准化特征
    scaler = StandardScaler()
    combined_features = scaler.fit_transform(combined_features)

    # 保存TfidfVectorizer和标准化器
    with open(vectorizer_file, 'wb') as f:
        pickle.dump(vectorizer, f)
    with open(scaler_file, 'wb') as f:
        pickle.dump(scaler, f)

    # 检查模型文件是否存在
    if os.path.exists(model_file):
        print("模型文件存在，直接加载模型。")
        autoencoder = load_model(model_file)
        input_dim = combined_features.shape[1]  # 确保加载模型时定义 input_dim
    else:
        print("模型文件不存在，开始训练模型。")
        # 定义卷积自编码器
        input_dim = combined_features.shape[1]
        input_layer = Input(shape=(input_dim, 1))

        # 编码器
        x = Conv1D(16, 3, activation='relu', padding='same')(input_layer)
        x = MaxPooling1D(2, padding='same')(x)
        x = Conv1D(8, 3, activation='relu', padding='same')(x)
        encoded = MaxPooling1D(2, padding='same')(x)

        # 解码器
        x = Conv1D(8, 3, activation='relu', padding='same')(encoded)
        x = UpSampling1D(2)(x)
        x = Conv1D(16, 3, activation='relu', padding='same')(x)
        x = UpSampling1D(2)(x)
        x = Conv1D(1, 3, activation='sigmoid', padding='same')(x)

        # 使用 Flatten 层将输出展平
        x = Flatten()(x)

        # 使用 Dense 层恢复到原始的输入维度
        decoded = Dense(input_dim)(x)  # input_dim 是合并特征后的维度

        # 定义自编码器模型
        autoencoder = Model(inputs=input_layer, outputs=decoded)

        # 编译模型
        autoencoder.compile(optimizer='adam', loss='mean_squared_error')

        # 训练自编码器
        combined_features_reshaped = combined_features.reshape(-1, input_dim, 1)
        autoencoder.fit(combined_features_reshaped, combined_features_reshaped, epochs=50, batch_size=256, shuffle=True)

        # 保存模型
        autoencoder.save(model_file)

    # 使用模型进行预测
    combined_features_reshaped = combined_features.reshape(-1, input_dim, 1)
    predicted_log = autoencoder.predict(combined_features_reshaped)

    # 调整预测结果的形状为 (batch_size, input_dim, 1)
    predicted_log = predicted_log.reshape(-1, input_dim, 1)

    # 计算每条日志的均方误差（MSE）
    mse = np.mean(np.square(combined_features_reshaped - predicted_log), axis=(1, 2))

    # 计算整个数据集的平均MSE和标准差
    mean_mse = np.mean(mse)
    std_mse = np.std(mse)

    # 计算阈值（平均值的0.5倍标准差）
    threshold = mean_mse + 0.5 * std_mse

    print(f"整个数据集的平均MSE：{mean_mse}")
    print(f"整个数据集的标准差：{std_mse}")
    print(f"推荐的阈值：{threshold}")

    return {
        "mean_mse": mean_mse,
        "std_mse": std_mse,
        "threshold": threshold,
    }