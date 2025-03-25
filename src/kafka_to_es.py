from elasticsearch import Elasticsearch
from kafka import KafkaConsumer
import json
import time
import os

# 配置ES和Kafka
ES_HOST = os.getenv('ES_HOST', 'http://100.118.100.15:9200')
KAFKA_BOOTSTRAP = os.getenv('KAFKA_BOOTSTRAP', '100.118.110.15:9092')
KAFKA_TOPIC = os.getenv('KAFKA_TOPIC', 'network_predictions')

es = Elasticsearch([ES_HOST])
INDEX_NAME = 'network_predictions'

consumer = KafkaConsumer(
    KAFKA_TOPIC,
    bootstrap_servers=KAFKA_BOOTSTRAP,
    group_id='es-writer',
    auto_offset_reset='earliest'
)

# 创建ES索引模板（若不存在）
if not es.indices.exists(index=INDEX_NAME):  # 使用关键字参数
    es.indices.create(
        index=INDEX_NAME,
        body={
            "mappings": {
                "properties": {
                    "timestamp": {"type": "date"},
                    "result": {"type": "float"},
                    "input": {"type": "object"}
                }
            }
        }
    )

for message in consumer:
    try:
        data = json.loads(message.value.decode('utf-8'))
        es.index(  # 所有参数均为关键字参数
            index=INDEX_NAME,
            document={
                "input": data['input'],
                "result": data['result'],
                "timestamp": data['timestamp']
            }
        )
        time.sleep(0.1)
    except Exception as e:
        print(f"Error processing message: {str(e)}")
