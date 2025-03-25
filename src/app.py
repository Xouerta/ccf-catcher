import os

from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
from os import environ, makedirs
import pandas as pd
import subprocess
import joblib
from kafka import KafkaProducer
import json

app = Flask(__name__)

# 配置数据库（使用SQLite作为示例，需替换为实际数据库）
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///predictions.db'  # 替换为MySQL/PostgreSQL
app.config['UPLOAD_FOLDER'] = 'uploads/'
makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)

db = SQLAlchemy(app)

KAFKA_TOPIC = environ.get('KAFKA_TOPIC', 'network_predictions')
KAFKA_BOOTSTRAP = environ.get('KAFKA_BOOTSTRAP', '100.118.110.15:9092')
producer = KafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP)

# 加载模型
MODEL_PATH = "E:/ccf-catcher/model/best_model_hgb_202503191724.joblib"
model = joblib.load(MODEL_PATH)


# 定义数据库模型
class PredictionRecord(db.Model):
    __tablename__ = 'prediction_records'
    id = db.Column(db.Integer, primary_key=True)
    input_data = db.Column(db.JSON)
    prediction = db.Column(db.Float)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)


# 新增上传PCAP文件接口
@app.route('/api/upload_pcap', methods=['POST'])
def upload_pcap():
    try:
        # 保存上传的PCAP文件
        file = request.files['file']
        pcap_path = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
        file.save(pcap_path)

        # 转换PCAP到CSV
        csv_path = os.path.splitext(pcap_path)[0] + '.csv'
        subprocess.run([
            'python',
            'pcap_transform/pcap_to_csv.py',
            pcap_path,
            csv_path
        ], check=True)

        # 读取CSV并预测
        df = pd.read_csv(csv_path)
        features = df[['len', 'source_port', 'dest_port']].astype(float)
        predictions = model.predict(features)

        # 存储到数据库和Kafka
        for idx, row in df.iterrows():
            data = row.to_dict()
            prediction = predictions[idx]
            record = PredictionRecord(input_data=data, prediction=prediction)
            db.session.add(record)
            db.session.commit()

            # 发送到Kafka
            producer.send(KAFKA_TOPIC, value=json.dumps({
                "input": data,
                "result": float(prediction),
                "timestamp": datetime.now().isoformat()
            }).encode('utf-8'))

        return jsonify({"status": "success", "message": "处理完成"})
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


# 原有预测接口保持不变
@app.route('/api/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        df = pd.DataFrame([data])
        prediction = model.predict(df)[0]

        record = PredictionRecord(input_data=data, prediction=prediction)
        db.session.add(record)
        db.session.commit()

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
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


@app.route('/api/history', methods=['GET'])
def get_history():
    records = PredictionRecord.query.order_by(PredictionRecord.created_at.desc()).limit(100).all()
    return jsonify([{
        "id": r.id,
        "input": r.input_data,
        "result": r.prediction,
        "timestamp": r.created_at.isoformat()
    } for r in records])


if __name__ == '__main__':
    with app.app_context():
        db.create_all()  # 初始化数据库表
    app.run(host='0.0.0.0', port=5000, debug=True)
