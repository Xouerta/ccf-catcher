import json
import os
from datetime import datetime

import joblib
import pandas as pd
from flask import Flask, request, jsonify
from kafka import KafkaProducer

app = Flask(__name__)

# Kafka配置
KAFKA_TOPIC = os.environ.get('KAFKA_TOPIC', 'network_predictions')
KAFKA_BOOTSTRAP = os.environ.get('KAFKA_BOOTSTRAP', '100.118.110.15:9092')
producer = KafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP)

MODEL_PATH = "E:/ccf-catcher/model/best_model_hgb_202503191724.joblib"
model = joblib.load(MODEL_PATH)

# 特征列（确保与 CSV 完全一致，包括前导空格）
feature_columns = [
    ' Destination Port', ' Flow Duration', ' Total Fwd Packets', ' Total Backward Packets',
    'Total Length of Fwd Packets', ' Total Length of Bwd Packets', ' Fwd Packet Length Max',
    ' Fwd Packet Length Min', ' Fwd Packet Length Mean', ' Fwd Packet Length Std',
    'Bwd Packet Length Max', ' Bwd Packet Length Min', ' Bwd Packet Length Mean',
    ' Bwd Packet Length Std', 'Flow Bytes/s', ' Flow Packets/s', ' Flow IAT Mean',
    ' Flow IAT Std', ' Flow IAT Max', ' Flow IAT Min', 'Fwd IAT Total', ' Fwd IAT Mean',
    ' Fwd IAT Std', ' Fwd IAT Max', ' Fwd IAT Min', 'Bwd IAT Total', ' Bwd IAT Mean',
    ' Bwd IAT Std', ' Bwd IAT Max', ' Bwd IAT Min', 'Fwd PSH Flags', ' Bwd PSH Flags',
    ' Fwd URG Flags', ' Bwd URG Flags', ' Fwd Header Length', ' Bwd Header Length',
    'Fwd Packets/s', ' Bwd Packets/s', ' Min Packet Length', ' Max Packet Length',
    ' Packet Length Mean', ' Packet Length Std', ' Packet Length Variance',
    'FIN Flag Count', ' SYN Flag Count', ' RST Flag Count', ' PSH Flag Count',
    ' ACK Flag Count', ' URG Flag Count', ' CWE Flag Count', ' ECE Flag Count',
    ' Down/Up Ratio', ' Average Packet Size', ' Avg Fwd Segment Size',
    ' Avg Bwd Segment Size', ' Fwd Header Length.1', 'Fwd Avg Bytes/Bulk',
    ' Fwd Avg Packets/Bulk', ' Fwd Avg Bulk Rate', ' Bwd Avg Bytes/Bulk',
    ' Bwd Avg Packets/Bulk', 'Bwd Avg Bulk Rate', 'Subflow Fwd Packets',
    ' Subflow Fwd Bytes', ' Subflow Bwd Packets', ' Subflow Bwd Bytes',
    'Init_Win_bytes_forward', ' Init_Win_bytes_backward', ' act_data_pkt_fwd',
    ' min_seg_size_forward', 'Active Mean', ' Active Std', ' Active Max',
    ' Active Min', 'Idle Mean', ' Idle Std', ' Idle Max', ' Idle Min'
]


def process_data(df):
    try:
        # 确保所有列均为数值类型
        df = df.apply(pd.to_numeric, errors='coerce').dropna()

        # 移除列名，仅传入数值数组（避免模型因列名不匹配报错）
        features = df.values.astype(float)
        predictions = model.predict(features)

        for idx, row in df.iterrows():
            data = row.to_dict()
            prediction = predictions[idx]
            producer.send(KAFKA_TOPIC, value=json.dumps({
                "input": data,
                "result": float(prediction),
                "timestamp": datetime.now().isoformat()
            }).encode('utf-8'))
    except Exception as e:
        print(f"处理数据失败: {str(e)}")
        print("当前列值:", df.columns.tolist())
        print("特征列数据类型:", df.dtypes)


def process_output_csv():
    csv_path = os.path.join('../proceed_files', 'output.csv')
    if os.path.exists(csv_path):
        df = pd.read_csv(csv_path)
        print("output.csv 列名:", df.columns.tolist())

        # 删除最后一列（标签列）
        df = df.iloc[:, :-1]

        # 验证列名匹配
        expected_columns = set(feature_columns)
        actual_columns = set(df.columns.tolist())
        missing_columns = expected_columns - actual_columns
        extra_columns = actual_columns - expected_columns

        print("缺失的列名:", missing_columns)
        print("多余的列名:", extra_columns)

        # 过滤非数值列并转换为数值类型
        df = df.apply(pd.to_numeric, errors='coerce').dropna()

        process_data(df)
    else:
        print("output.csv 文件不存在")


@app.route('/api/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        df = pd.DataFrame([data])

        # 过滤非数值列并严格匹配 feature_columns
        df = df[feature_columns]

        # 转换为数值类型并处理非数值数据
        df = df.apply(pd.to_numeric, errors='coerce').dropna()

        # 移除列名，仅传入数值数组
        features = df.values.astype(float)
        prediction = model.predict(features)[0]

        producer.send(KAFKA_TOPIC, value=json.dumps({
            "input": data,
            "result": prediction,
            "timestamp": datetime.now().isoformat()
        }).encode('utf-8'))

        return jsonify({
            "status": "success",
            "prediction": prediction
        })
    except Exception as e:
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    from apscheduler.schedulers.background import BackgroundScheduler

    scheduler = BackgroundScheduler()
    scheduler.add_job(process_output_csv, 'interval', minutes=1, id='process_output')
    scheduler.start()

    try:
        app.run(host='0.0.0.0', port=5000, debug=True)
    except KeyboardInterrupt:
        scheduler.shutdown()
