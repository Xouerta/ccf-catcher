from confluent_kafka import Producer
import logging
import json
from datetime import datetime

# 配置日志
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

kafka_ip = 'localhost'

class DeepSeekProducer:
    """专用的 Kafka 生产者"""

    def __init__(self):
        self.config = {
            'bootstrap.servers': f'{kafka_ip}:9092',
            'client.id': 'deepseek_producer',
            'message.send.max.retries': 3,
            'retry.backoff.ms': 1000
        }
        self.producer = Producer(self.config)

    def delivery_report(self, err, msg):
        """消息投递回调"""
        if err is not None:
            logger.error(f"消息投递失败: {err}")
        else:
            logger.info(f"消息已发送到 [{msg.topic()}] 分区 [{msg.partition()}]")

    def send_result(self, topic, value):
        """发送分析结果到指定主题"""
        try:
            # 将数据转换为 JSON 格式
            json_data = json.dumps({
                "timestamp": datetime.now().isoformat(),
                "data": value['data'],
                "result": value['result'],
                "source": "deepseek_analyzer"
            })

            self.producer.produce(
                topic=topic,
                value=json_data.encode('utf-8'),  # 编码 JSON 字符串
                callback=self.delivery_report
            )
            self.producer.poll(0)  # 触发回调
            self.producer.flush()
        except Exception as e:
            logger.error(f"Kafka 发送异常: {e}")


# 单例生产者实例
deepseek_producer = DeepSeekProducer()
