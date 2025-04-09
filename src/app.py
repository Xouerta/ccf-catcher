import json
import os
from datetime import datetime

import joblib
import pandas as pd
from flask import Flask, request, jsonify
from kafka_producer import kafka_producer
import json
import random

app = Flask(__name__)

# Kafka配置
KAFKA_TOPIC = os.environ.get('KAFKA_TOPIC', 'network_predictions')
KAFKA_BOOTSTRAP = os.environ.get('KAFKA_BOOTSTRAP', '100.118.110.15:9092')

MODEL_PATH = "model/best_model_hgb_202503191724.joblib"
model = joblib.load(MODEL_PATH)

# 修改后的feature_columns
feature_columns = [
    'FlowDuration', 'DestinationPort', 'TotalFwdPackets',
    'TotalBackwardPackets', 'TotalLengthOfFwdPackets',
    'TotalLengthOfBwdPackets', 'FwdPacketLengthMax', 'FwdPacketLengthMin',
    'FwdPacketLengthMean', 'FwdPacketLengthStd', 'BwdPacketLengthMax',
    'BwdPacketLengthMin', 'BwdPacketLengthMean', 'BwdPacketLengthStd',
    'FlowBytes', 'FlowPackets', 'FlowIATMean', 'FlowIATStd', 'FlowIATMax',
    'FlowIATMin', 'FwdIATTotal', 'FwdIATMean', 'FwdIATStd', 'FwdIATMax',
    'FwdIATMin', 'BwdIATTotal', 'BwdIATMean', 'BwdIATStd', 'BwdIATMax',
    'BwdIATMin', 'FwdPSHFlags', 'BwdPSHFlags', 'FwdURGFlags', 'BwdURGFlags',
    'FwdHeaderLength', 'BwdHeaderLength', 'FwdPackets', 'BwdPackets',
    'MinPacketLength', 'MaxPacketLength', 'PacketLengthMean',
    'PacketLengthStd', 'PacketLengthVariance', 'FINFlagCount',
    'SYNFlagCount', 'RSTFlagCount', 'PSHFlagCount', 'ACKFlagCount',
    'URGFlagCount', 'CWEFlagCount', 'ECEFlagCount', 'DownUpRatio',
    'AveragePacketSize', 'AvgFwdSegmentSize', 'AvgBwdSegmentSize',
    'FwdHeaderLength1', 'FwdAvgBytesBulk', 'FwdAvgPacketsBulk',
    'FwdAvgBulkRate', 'BwdAvgBytesBulk', 'BwdAvgPacketsBulk',
    'BwdAvgBulkRate', 'SubflowFwdPackets', 'SubflowFwdBytes',
    'SubflowBwdPackets', 'SubflowBwdBytes', 'Init_Win_bytes_forward',
    'Init_Win_bytes_backward', 'act_data_pkt_fwd', 'min_seg_size_forward',
    'ActiveMean', 'ActiveStd', 'ActiveMax', 'ActiveMin', 'IdleMean',
    'IdleStd', 'IdleMax', 'IdleMin'
]


def process_data(df):
    try:
        # 确保所有列均为数值类型
        df = df.apply(pd.to_numeric, errors='coerce').dropna()

        # 移除列名，仅传入数值数组（避免模型因列名不匹配报错）
        features = df.values.astype(float)
        predictions = model.predict(features)

        min_samples = 1  # 最小发送条数
        max_samples = 10 # 最大发送条数
        num_samples = random.randint(min_samples, max_samples)  # 随机生成发送条数

        print(f"发送 {num_samples} 条数据到 Kafka")
        random_indices = random.sample(range(len(df)), num_samples)

        for idx in random_indices:
            row = df.iloc[idx]
            data = row.to_dict()
            prediction = predictions[idx]

            value = {
                "input": json.dumps(data),
                "result": str(prediction),
                "timestamp": datetime.now().isoformat()
            }

            # 配置JSON序列化
            message = json.dumps(
                value,
                ensure_ascii=False,  # 允许非ASCII字符
                default=str  # 处理无法序列化的对象
            )
            kafka_producer.send_result(KAFKA_TOPIC, value=message)

    except Exception as e:
        print(f"处理数据失败: {str(e)}")
        # print("当前列值:", df.columns.tolist())
        # print("特征列数据类型:", df.dtypes)


def process_output_csv():
    csv_path = os.path.join('../proceed_files', 'output_cleaned.csv')
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
        app.run(host='0.0.0.0', port=6000, debug=True)
    except KeyboardInterrupt:
        scheduler.shutdown()
