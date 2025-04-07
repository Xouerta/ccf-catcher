from sklearn.feature_extraction.text import TfidfVectorizer
import joblib
import os
import numpy as np
import re
import math
from collections import Counter

def calculate_entropy(password):
    """计算密码熵值"""
    char_counts = Counter(password)
    length = len(password)
    if length == 0:
        return 0.0
    entropy = 0.0
    for count in char_counts.values():
        p = count / length
        entropy -= p * math.log2(p)
    return entropy * length

def extract_text_features(passwords, train_mode=True):
    vectorizer_path = "../models/TFidf.pkl"
    
    # TF-IDF特征
    if train_mode:
        vectorizer = TfidfVectorizer(
            analyzer='char_wb',
            ngram_range=(1, 4),
            max_features=2000,
            lowercase=False,
            min_df=0.001
        )
        X_text = vectorizer.fit_transform(passwords)
        joblib.dump(vectorizer, vectorizer_path)
    else:
        vectorizer = joblib.load(vectorizer_path)
        X_text = vectorizer.transform(passwords)
    
    # 统计特征
    stats_features = []
    for pwd in passwords:
        pwd_len = len(pwd)
        has_upper = int(any(c.isupper() for c in pwd))
        has_lower = int(any(c.islower() for c in pwd))
        has_digit = int(any(c.isdigit() for c in pwd))
        has_special = int(any(c in '!@#$%^&*(),.?":{}|<>' for c in pwd))
        repeat_pattern = int(bool(re.search(r'(.)\1{2,}', pwd)))  # 重复字符
        
        # 比例特征
        digit_ratio = sum(c.isdigit() for c in pwd)/pwd_len if pwd_len>0 else 0
        upper_ratio = sum(c.isupper() for c in pwd)/pwd_len if pwd_len>0 else 0
        unique_ratio = len(set(pwd))/pwd_len if pwd_len>0 else 0
        
        stats_features.append([
            pwd_len,
            has_upper, has_lower, has_digit, has_special,
            digit_ratio, upper_ratio, unique_ratio,
            repeat_pattern,
            calculate_entropy(pwd)  # 密码熵
        ])
    
    return np.hstack((X_text.toarray(), np.array(stats_features)))