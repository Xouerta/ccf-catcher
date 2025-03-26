import json
import os
import subprocess
import tempfile  # 临时文件处理
from datetime import datetime
from os import environ, makedirs

import joblib
import pandas as pd
import requests  # HTTP请求库
from apscheduler.schedulers.background import BackgroundScheduler  # 定时任务库
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from kafka import KafkaProducer
from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer

app = Flask(__name__)

# 配置数据库（使用SQLite作为示例，需替换为实际数据库）
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///predictions.db'  # 替换为MySQL/PostgreSQL
app.config['UPLOAD_FOLDER'] = 'uploads/'
app.config['INPUT_FOLDER'] = 'input_files/'  # 实时监控目录
app.config['ARCHIVE_FOLDER'] = 'processed_files/'  # 处理完成后归档目录
app.config['API_PCAP_URL'] = environ.get('API_PCAP_URL', 'http://security-api.example.com/pcap')  # 安全设施API地址
app.config['API_TOKEN'] = environ.get('API_TOKEN', 'your_token_here')  # API认证Token

makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
makedirs(app.config['INPUT_FOLDER'], exist_ok=True)
makedirs(app.config['ARCHIVE_FOLDER'], exist_ok=True)

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


# ---------------- 文件监控类 ----------------
class PCAPFileHandler(FileSystemEventHandler):
    """监控pcap文件创建事件"""

    def on_created(self, event):
        if event.src_path.endswith('.pcap'):
            # 调用统一处理函数
            process_pcap_file(event.src_path)


# ---------------- 新增API请求函数 ----------------
def fetch_pcap_from_api():
    """定时从安全设施API拉取pcap文件"""
    try:
        headers = {'Authorization': f'Bearer {app.config["API_TOKEN"]}'}
        response = requests.get(
            app.config['API_PCAP_URL'],
            headers=headers,
            timeout=10
        )

        if response.status_code == 200 and response.headers.get('Content-Type') == 'application/octet-stream':
            # 保存为临时文件
            with tempfile.NamedTemporaryFile(
                    dir=app.config['INPUT_FOLDER'],
                    suffix='.pcap',
                    delete=False
            ) as temp:
                temp.write(response.content)
                temp_path = temp.name
                print(f"成功保存API获取的pcap文件: {temp_path}")
                # 触发处理流程
                process_pcap_file(temp_path)
        else:
            print(f"API请求失败: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"API请求异常: {str(e)}")


# ---------------- 公共处理函数 ----------------
def process_pcap_file(pcap_path):
    """统一处理pcap文件的函数（被HTTP接口、文件监控和API调用触发）"""
    try:
        # 转换pcap到csv
        csv_path = os.path.splitext(pcap_path)[0] + '.csv'
        subprocess.run([
            'python',
            'pcap_transform/pcap_to_csv.py',
            pcap_path,
            csv_path
        ], check=True)

        # 预测与存储逻辑
        df = pd.read_csv(csv_path)
        features = df[['len', 'source_port', 'dest_port']].astype(float)
        predictions = model.predict(features)

        for idx, row in df.iterrows():
            data = row.to_dict()
            prediction = predictions[idx]
            record = PredictionRecord(input_data=data, prediction=prediction)
            db.session.add(record)

        db.session.commit()  # 批量提交（减少数据库操作）

        # 发送到Kafka
        for idx, row in df.iterrows():
            data = row.to_dict()
            prediction = predictions[idx]
            producer.send(KAFKA_TOPIC, value=json.dumps({
                "input": data,
                "result": float(prediction),
                "timestamp": datetime.now().isoformat()
            }).encode('utf-8'))

    except Exception as e:
        db.session.rollback()
        print(f"处理失败: {str(e)}")
    finally:
        # 将文件移动到归档目录（避免重复处理）
        archive_path = os.path.join(
            app.config['ARCHIVE_FOLDER'],
            os.path.basename(pcap_path)
        )
        os.rename(pcap_path, archive_path)


# ---------------- HTTP接口 ----------------
@app.route('/api/upload_pcap', methods=['POST'])
def upload_pcap():
    try:
        file = request.files['file']
        pcap_path = os.path.join(app.config['UPLOAD_FOLDER'], file.filename)
        file.save(pcap_path)
        # 调用统一处理函数
        process_pcap_file(pcap_path)
        return jsonify({"status": "success", "message": "处理完成"})
    except Exception as e:
        db.session.rollback()
        return jsonify({"error": str(e)}), 500


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
    records = PredictionRecord.query.order_by(
        PredictionRecord.created_at.desc()
    ).limit(100).all()
    return jsonify([{
        "id": r.id,
        "input": r.input_data,
        "result": r.prediction,
        "timestamp": r.created_at.isoformat()
    } for r in records])


# ---------------- 应用启动 ----------------
if __name__ == '__main__':
    with app.app_context():
        db.create_all()  # 初始化数据库表

    # 启动文件监控
    event_handler = PCAPFileHandler()
    observer = Observer()
    observer.schedule(
        event_handler,
        path=app.config['INPUT_FOLDER'],
        recursive=False
    )
    observer.start()

    # 启动API定时任务（每5秒拉取一次）
    scheduler = BackgroundScheduler()
    scheduler.add_job(
        fetch_pcap_from_api,
        'interval',
        seconds=5,  # 根据需求调整间隔时间
        id='fetch_pcap_api'
    )
    scheduler.start()

    try:
        app.run(host='0.0.0.0', port=5000, debug=True)
    except KeyboardInterrupt:
        observer.stop()
        scheduler.shutdown()
    finally:
        observer.join()
