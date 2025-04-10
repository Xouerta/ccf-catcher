# -*- coding: utf-8 -*-
import os
import numpy as np
import pandas as pd
from collections import Counter
from scipy.stats import entropy
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
import joblib
from sklearn.metrics import confusion_matrix, roc_curve, auc
import matplotlib.pyplot as plt
import logging
import lightgbm as lgb

def calculate_entropy(data):
    if not data:
        return 0
    counts = Counter(data)
    probs = [count / len(data) for count in counts.values()]
    return entropy(probs, base=2)


def extract_features(file_path):
    with open(file_path, 'rb') as f:
        data = f.read()

    byte_counts = Counter(data)
    most_common = byte_counts.most_common(20)
    byte_freq = [count[1] / len(data) if len(data) > 0 else 0 for count in most_common]

    # 新增特征
    unique_bytes = len(byte_counts)
    printable_ratio = sum(32 <= b <= 126 for b in byte_counts) / len(data) if len(data) > 0 else 0
    suspicious_strings = sum(b in [60, 62, 63, 64] for b in byte_counts)  # <, >, ?, @
    control_chars = sum(b < 32 or b == 127 for b in byte_counts)  # 控制字符
    long_strings = sum(1 for b in data if b == 0)  # 长字符串数量
    magic_numbers = sum(1 for b in data[:4] if b in [0x7F, 0x4D, 0x5A])  # 文件头特征

    # 新增统计特征
    mean_byte = np.mean(list(byte_counts.keys())) if byte_counts else 0
    std_byte = np.std(list(byte_counts.keys())) if byte_counts else 0
    skewness = pd.Series(list(byte_counts.keys())).skew() if byte_counts else 0  # 偏度
    kurtosis = pd.Series(list(byte_counts.keys())).kurt() if byte_counts else 0  # 峰度

    # 新增文件结构特征
    header_entropy = calculate_entropy(data[:100])  # 文件头100字节的熵
    footer_entropy = calculate_entropy(data[-100:]) if len(data) > 100 else 0  # 文件尾100字节的熵
    section_ratio = len(data) / (unique_bytes + 1)  # 文件大小与唯一字节数的比值

    # 新增字符串特征
    ascii_strings = sum(1 for b in data if 32 <= b <= 126)  # ASCII字符串数量
    unicode_strings = sum(1 for b in data if b > 127)  # Unicode字符串数量

    # 新增更多特征
    # 1. 文件分段特征
    segment_size = 512  # 每段512字节
    segments = [data[i:i + segment_size] for i in range(0, len(data), segment_size)]
    segment_entropies = [calculate_entropy(seg) for seg in segments]
    max_segment_entropy = max(segment_entropies) if segment_entropies else 0
    min_segment_entropy = min(segment_entropies) if segment_entropies else 0
    avg_segment_entropy = np.mean(segment_entropies) if segment_entropies else 0

    # 2. 特殊字符组合
    php_open = sum(1 for i in range(len(data) - 4) if data[i:i + 5] == b'<?php')  # PHP开始标记
    php_close = sum(1 for i in range(len(data) - 2) if data[i:i + 3] == b'?>')  # PHP结束标记
    jsp_open = sum(1 for i in range(len(data) - 4) if data[i:i + 5] == b'<%--')  # JSP开始标记
    jsp_close = sum(1 for i in range(len(data) - 2) if data[i:i + 3] == b'--%>')  # JSP结束标记
    asp_open = sum(1 for i in range(len(data) - 2) if data[i:i + 3] == b'<%=')  # ASP开始标记
    asp_close = sum(1 for i in range(len(data) - 2) if data[i:i + 3] == b'%>')  # ASP结束标记
    js_eval = sum(1 for i in range(len(data) - 4) if data[i:i + 5] == b'eval(')  # JS eval函数
    py_exec = sum(1 for i in range(len(data) - 4) if data[i:i + 5] == b'exec(')  # Python exec函数

    # 3. 文件类型特征
    is_executable = 1 if data[:4] in [b'\x7fELF', b'MZ\x90\x00'] else 0  # ELF或PE可执行文件
    is_script = 1 if any(marker in data[:100] for marker in [b'#!/', b'<?php', b'<%--', b'<%=']) else 0  # 脚本文件

    features = byte_freq + [0] * (20 - len(byte_freq))
    features.extend([
        calculate_entropy(data),
        len(data) / (1024 * 1024) if len(data) > 0 else 0,
        unique_bytes,
        printable_ratio,
        suspicious_strings,
        control_chars,
        long_strings,
        magic_numbers,
        mean_byte,
        std_byte,
        skewness,
        kurtosis,
        header_entropy,
        footer_entropy,
        section_ratio,
        ascii_strings,
        unicode_strings,
        max_segment_entropy,
        min_segment_entropy,
        avg_segment_entropy,
        php_open,
        php_close,
        jsp_open,
        jsp_close,
        asp_open,
        asp_close,
        js_eval,
        py_exec,
        is_executable,
        is_script
    ])

    return features


def build_dataset(data_dir):
    features = []
    labels = []
    for category in ['webshell', 'benign']:
        folder = os.path.join(data_dir, category)
        for filename in os.listdir(folder):
            file_path = os.path.join(folder, filename)
            try:
                feat = extract_features(file_path)
                features.append(feat)
                labels.append(1 if category == 'webshell' else 0)
            except Exception as e:
                logging.info(f"Error processing {file_path}: {e}")

    from imblearn.over_sampling import SMOTE
    smote = SMOTE(random_state=42)
    features, labels = smote.fit_resample(features, labels)

    df = pd.DataFrame(features)
    df['label'] = labels
    return df


def main():
    df = build_dataset('./data')
    df = df.drop_duplicates()

    # 检查特征值
    logging.info("特征统计信息:")
    logging.info(df.describe())

    # 检查类别分布
    logging.info("类别分布:")
    logging.info(df['label'].value_counts(normalize=True))

    X = df.drop('label', axis=1)
    y = df['label']
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    clf = lgb.LGBMClassifier(
       objective='binary',
        metric='auc',
        boosting_type='goss',  # 更快的训练方式
        n_estimators=1000,
        max_depth=15,          # 减小深度防止过拟合
        learning_rate=0.01,    # 更小的学习率
        num_leaves=63,
        min_child_samples=10,  # 更严格的过拟合控制
        feature_fraction=0.8,
        bagging_freq=5,
        verbosity=-1
    )

    # 训练模型（修正后的版本）
    clf.fit(
        X_train,
        y_train,
        eval_set=[(X_test, y_test)],
        eval_metric='auc',
        callbacks=[
            lgb.early_stopping(stopping_rounds=50),  # 早停回调
            lgb.log_evaluation(10)  # 每10轮输出日志
        ]
    )
    # 训练模型
    clf.fit(X_train, y_train)

    y_pred = clf.predict(X_test)
    y_pred_proba = clf.predict_proba(X_test)[:, 1]  # 获取预测概率
    y_pred = (y_pred_proba > 0.4).astype(int)  # 将阈值从0.5调整为0.4

    # 输出准确率
    logging.info(f"准确率: {accuracy_score(y_test, y_pred)}")

    # 输出分类报告
    logging.info(f"分类报告:\n {classification_report(y_test, y_pred)}")

    # 输出混淆矩阵
    cm = confusion_matrix(y_test, y_pred)
    logging.info(f"混淆矩阵:\n{cm}")

    # 计算并输出ROC AUC
    fpr, tpr, thresholds = roc_curve(y_test, y_pred_proba)
    roc_auc = auc(fpr, tpr)
    logging.info(roc_auc)
    logging.info(f"ROC AUC: {roc_auc:.4f}")

    plt.figure()
    plt.plot(fpr, tpr, color='darkorange', lw=2, label=f'ROC curve (area = {roc_auc:.2f})')
    plt.plot([0, 1], [0, 1], color='navy', lw=2, linestyle='--')
    plt.xlabel('false')
    plt.ylabel('true')
    plt.title('ROC')
    plt.legend(loc="lower right")
    plt.savefig('roc_curve.png')
    plt.close()

    joblib.dump({
        'model': clf,
        'feature_names': X_train.columns.tolist()
    }, 'malware_detector_lightgbm.pkl')


if __name__ == "__main__":
    main()
