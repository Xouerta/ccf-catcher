import pandas as pd
import joblib
import os
import sys
import numpy as np
from feature import extract_text_features

def load_model(model_path):
    if not os.path.exists(model_path):
        print(f"❌ 未找到模型文件 {model_path}")
        sys.exit(1)
    try:
        return joblib.load(model_path)
    except Exception as e:
        print(f"模型加载失败: {str(e)}")
        sys.exit(1)

# 配置路径
MODEL_PATH = "../models/LGBM.pkl"  # 修改点1：使用LightGBM模型
TEST_DATA_PATH = "../data/test_passwords.csv"
OUTPUT_PATH = "../data/predicted_results.csv"

# 加载模型
model = load_model(MODEL_PATH)

# 加载数据
if not os.path.exists(TEST_DATA_PATH):
    pd.DataFrame(columns=["password"]).to_csv(TEST_DATA_PATH, index=False)
    print("❌ 测试文件不存在，已创建空模板")
    sys.exit()

df_test = pd.read_csv(TEST_DATA_PATH)
if df_test.empty or "password" not in df_test.columns:
    print("❌ 测试数据格式错误")
    sys.exit()

# 特征提取
try:
    passwords = df_test["password"].dropna()
    X_new = extract_text_features(passwords, train_mode=False)
except Exception as e:
    print(f"特征提取失败: {str(e)}")
    sys.exit()

# 分批预测（修改点2：增加空数据检查）
if X_new.shape[0] > 0:
    batch_size = 1000
    predictions = []
    for i in range(0, X_new.shape[0], batch_size):
        batch = X_new[i:i+batch_size]
        predictions.extend(model.predict(batch))
else:
    predictions = []

# 保存结果
df_test["prediction"] = predictions[:len(df_test)]  # 对齐长度
df_test.to_csv(OUTPUT_PATH, index=False)
print(f"✅ 预测完成！结果保存至 {OUTPUT_PATH}")
print(df_test.head(10))