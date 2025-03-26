import json
import os
import subprocess
import tempfile
from datetime import datetime
from os import environ, makedirs

import joblib
import pandas as pd
import requests
from apscheduler.schedulers.background import BackgroundScheduler
from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from kafka import KafkaProducer
from watchdog.events import FileSystemEventHandler
from watchdog.observers import Observer

app = Flask(__name__)

# 配置数据库
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///predictions.db'
app.config['UPLOAD_FOLDER'] = 'uploads/'
app.config['INPUT_FOLDER'] = 'input_files/'
app.config['ARCHIVE_FOLDER'] = 'processed_files/'
app.config['API_PCAP_URL'] = environ.get('API_PCAP_URL', 'http://security-api.example.com/pcap')
app.config['API_TOKEN'] = environ.get('API_TOKEN', 'your_token_here')

makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
makedirs(app.config['INPUT_FOLDER'], exist_ok=True)
makedirs(app.config['ARCHIVE_FOLDER'], exist_ok=True)

db = SQLAlchemy(app)

KAFKA_TOPIC = environ.get('KAFKA_TOPIC', 'network_predictions')
KAFKA_BOOTSTRAP = environ.get('KAFKA_BOOTSTRAP', '100.118.110.15:9092')
producer = KafkaProducer(bootstrap_servers=KAFKA_BOOTSTRAP)

MODEL_PATH = "E:/ccf-catcher/model/best_model_hgb_202503191724.joblib"
model = joblib.load(MODEL_PATH)


class PredictionRecord(db.Model):
    __tablename__ = 'prediction_records'
    id = db.Column(db.Integer, primary_key=True)
    input_data = db.Column(db.JSON)
    prediction = db.Column(db.Float)
    created_at = db.Column(db.DateTime, default=datetime.utcnow)


# ---------------- 文件监控类 ----------------
class PCAPFileHandler(FileSystemEventHandler):
    def on_created(self, event):
        if event.src_path.endswith('.pcap'):
            process_pcap_file(event.src_path)


# ---------------- 新增公用处理函数 ----------------
def process_data(df):
    """统一处理数据的逻辑（数据库存储和Kafka发送）"""
    try:
        features = df[['len', 'source_port', 'dest_port']].astype(float)
        predictions = model.predict(features)

        # 存储到数据库
        for idx, row in df.iterrows():
            data = row.to_dict()
            prediction = predictions[idx]
            record = PredictionRecord(input_data=data, prediction=prediction)
            db.session.add(record)
        db.session.commit()

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
        print(f"处理数据失败: {str(e)}")


# ---------------- API请求与处理逻辑 ----------------
def fetch_pcap_from_api():
    """定时从API拉取pcap并处理，失败时处理本地CSV"""
    try:
        headers = {'Authorization': f'Bearer {app.config["API_TOKEN"]}'}
        response = requests.get(
            app.config['API_PCAP_URL'],
            headers=headers,
            timeout=10
        )

        if response.status_code == 200 and response.headers.get('Content-Type') == 'application/octet-stream':
            # 保存并处理API获取的pcap
            with tempfile.NamedTemporaryFile(
                    dir=app.config['INPUT_FOLDER'],
                    suffix='.pcap',
                    delete=False
            ) as temp:
                temp.write(response.content)
                temp_path = temp.name
                process_pcap_file(temp_path)
        else:
            print(f"API请求失败: {response.status_code} - {response.text}")
            # API失败时处理本地CSV
            handle_backup_csv()

    except Exception as e:
        print(f"API请求异常: {str(e)}")
        handle_backup_csv()


def handle_backup_csv():
    """处理备用CSV文件（processed_files/output.csv）"""
    backup_csv_path = os.path.join(
        app.config['ARCHIVE_FOLDER'],
        'output.csv'
    )
    if os.path.exists(backup_csv_path):
        df = pd.read_csv(backup_csv_path)
        process_data(df)
    else:
        print("备用CSV文件不存在")


# ---------------- 处理pcap文件 ----------------
def process_pcap_file(pcap_path):
    """转换pcap到CSV并处理数据"""
    try:
        csv_path = os.path.splitext(pcap_path)[0] + '.csv'
        subprocess.run([
            'python',
            'pcap_transform/pcap_to_csv.py',
            pcap_path,
            csv_path
        ], check=True)

        df = pd.read_csv(csv_path)
        process_data(df)

    except Exception as e:
        db.session.rollback()
        print(f"处理pcap失败: {str(e)}")
    finally:
        # 归档pcap文件
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
        process_pcap_file(pcap_path)
        return jsonify({"status": "success", "message": "处理完成"})
    except Exception as e:
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
        db.create_all()

    # 启动文件监控
    event_handler = PCAPFileHandler()
    observer = Observer()
    observer.schedule(event_handler, path=app.config['INPUT_FOLDER'], recursive=False)
    observer.start()

    # 启动API定时任务（每5秒拉取一次）
    scheduler = BackgroundScheduler()
    scheduler.add_job(
        fetch_pcap_from_api,
        'interval',
        seconds=5,
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
