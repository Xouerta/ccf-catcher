import pandas as pd
import joblib
import zxcvbn
import numpy as np
import re
from lightgbm import LGBMClassifier, early_stopping, log_evaluation
from sklearn.model_selection import train_test_split, StratifiedKFold
from sklearn.metrics import classification_report, roc_auc_score, confusion_matrix
from skopt import BayesSearchCV
from feature import extract_text_features

# 加载基础数据集
df = pd.read_csv("../data/password_dataset.csv").dropna(subset=["password"])

# 加载强密码数据集
strong_passwords = []
try:
    with open("../data/rockyou.txt", "r", encoding="latin-1") as f:
        for line in f:
            pwd = line.strip()
            if 12 <= len(pwd) <= 64:
                strong_passwords.append(pwd)
except FileNotFoundError:
    print("⚠️ 未找到 rockyou.txt 文件，仅使用基础数据集。")

# 创建 DataFrame 并标记标签
strong_df = pd.DataFrame({
    "password": strong_passwords,
    "label": 1  # 强密码标记为 1
})
df = pd.concat([df, strong_df], ignore_index=True)

# 生成密码强度标签
def generate_label(password):
    result = zxcvbn.zxcvbn(password)
    return 1 if result['score'] >= 3 else 0

print("⏳ 正在生成密码强度标签...")
df["label"] = df["password"].apply(generate_label)

# 特征提取
print("🔍 正在提取密码特征...")
X = extract_text_features(df["password"])
y = df["label"]

# 划分训练集和测试集
X_train, X_test, y_train, y_test = train_test_split(
    X, y, 
    test_size=0.2, 
    stratify=y,
    random_state=42
)

# 贝叶斯优化的参数范围
param_space = {
    'num_leaves': (30, 150),  # 叶子节点数
    'learning_rate': (0.001, 0.2),  # 学习率
    'min_child_samples': (20, 200),  # 叶子最小样本数
    'reg_alpha': (0, 2),  # L1 正则化
    'reg_lambda': (0, 2),  # L2 正则化
    'colsample_bytree': (0.7, 1.0),  # 每次迭代的列采样比例
    'min_split_gain': (0.0, 0.5)  # 最小分割增益
}

# 初始化 LightGBM 模型
model = LGBMClassifier(
    objective='binary',  # 目标是二分类
    n_estimators=1000,  # 最大迭代次数
    class_weight='balanced',  # 自动调整类别权重
    random_state=42,
    n_jobs=-1,
    verbose=-1  # 关闭 LightGBM 日志输出
)

# 进行贝叶斯优化搜索
opt = BayesSearchCV(
    estimator=model,
    search_spaces=param_space,
    n_iter=50,  # 搜索 50 组参数
    cv=StratifiedKFold(n_splits=5, shuffle=True),  # 5 折交叉验证
    scoring='roc_auc',  # 评价指标：AUC
    verbose=0,  # 关闭冗余日志
    n_jobs=-1  # 使用所有 CPU 进行计算
)

# 开始训练
print("🚀 开始训练模型，请耐心等待...")
opt.fit(
    X_train, y_train,
    eval_set=[(X_test, y_test)],
    eval_metric='auc',  # 评估指标：AUC
    callbacks=[
        early_stopping(stopping_rounds=20, verbose=1),  # 早停
        log_evaluation(period=50)  # 每 50 轮记录一次日志
    ]
)

# 获取最佳模型
best_model = opt.best_estimator_
print("\n🎯 最佳模型参数:")
for param, value in opt.best_params_.items():
    print(f"  - {param}: {value}")

# 评估模型
y_pred = best_model.predict(X_test)
y_proba = best_model.predict_proba(X_test)[:, 1]

print(f"\n📊 测试集 AUC(曲线下面积): {roc_auc_score(y_test, y_proba):.4f}")
print("\n📝 分类报告(precision, recall, f1-score):")
print(classification_report(y_test, y_pred))

print("\n🔢 混淆矩阵（预测 vs 真实）:")
print(confusion_matrix(y_test, y_pred))

# 保存模型
joblib.dump(best_model, "../models/lgbm.pkl")
print("\n✅ 训练完成，模型已保存至: ../models/lgbm.pkl")
