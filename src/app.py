import json
import os
from datetime import datetime

import joblib
import pandas as pd
from flask import Flask, request, jsonify
from kafka import KafkaProducer

app = Flask(__name__)

# Kafka配置（优先使用环境变量）
KAFKA_TOPIC = os.environ.get('KAFKA_TOPIC', 'network_predictions')
KAFKA_BOOTSTRAP = os.environ.get('KAFKA_BOOTSTRAP', '100.118.110.15:9092')  # 修改为本地测试
producer = KafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP)

MODEL_PATH = "E:/ccf-catcher/model/best_model_hgb_202503191724.joblib"
model = joblib.load(MODEL_PATH)


def process_data(df):
    try:
        features = df[['len', 'source_port', 'dest_port']].astype(float)
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


def process_output_csv():
    csv_path = os.path.join('proceed_files', 'output.csv')
    if os.path.exists(csv_path):
        df = pd.read_csv(csv_path)
        process_data(df)
    else:
        print("output.csv 文件不存在")


@app.route('/api/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        df = pd.DataFrame([data])
        prediction = model.predict(df)[0]

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
