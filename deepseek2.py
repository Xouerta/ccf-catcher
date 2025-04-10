import os
import platform
import time
import logging
import subprocess
import threading
from flask import Flask, jsonify
from flask_socketio import SocketIO
import requests

from kafka_producer import deepseek_producer

# 配置日志
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

# 初始化 Flask & SocketIO
app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*", async_mode="threading")


# API 配置
DEEPSEEK_API_URL = "https://api.deepseek.com"
DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", 'sk-2b3d0d01a3a94b268a6417f420cb6653')

# 线程管理
stop_event = threading.Event()

class DeepSeekClient:
    """DeepSeek API 客户端"""
    def analyze_log(self, message):
        logger.info(f"tail  {message}")
        data = {
            'data': message,
            'result': 'network_error'
        }
        try:
            response = requests.post(
                f"{DEEPSEEK_API_URL}/v1/chat/completions",
                json={
                    "model": "deepseek-chat",
                    "messages": [{"role": "system", "content": "判断日志是否为攻击行为，仅返回 true 或 false"},
                                 {"role": "user", "content": message}],
                },
                headers={"Authorization": f"Bearer {DEEPSEEK_API_KEY}"},
                timeout=5  # 5秒超时，避免阻塞
            )
            result = response.json()

            data = {
                'data': message,
                'result': result.get("choices", [{}])[0].get("message", {}).get("content", "Error")
            }
            deepseek_producer.send_result('log_security', data)
            logger.info(f"kafka  ->  {message}")

        except Exception as e:
            deepseek_producer.send_result('log_security', data)
            logger.error(f"DeepSeek API 请求失败: {str(e)} ")

def read_linux_log():
    """Linux 日志监控"""
    deepseek_client = DeepSeekClient()
    log_files = [
        "/var/log/auth.log",
        "/var/log/syslog",
        "/var/log/message.log",
        "/var/log/ufw.log",
    ]
    log_buffer = []  # 缓存多个日志

    # 检查日志文件是否存在
    for log_file_path in log_files:
        if not os.path.exists(log_file_path):
            logger.error(f"日志文件 {log_file_path} 不存在。")
            continue

    # 读取每个日志文件
    processes = []
    for log_file_path in log_files:
        if os.path.exists(log_file_path):
            process = subprocess.Popen(
                ["tail", "-n", "0", "-F", log_file_path],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True
            )
            processes.append((process, log_file_path))

    try:
        while not stop_event.is_set():
            for process, log_file_path in processes:
                line = process.stdout.readline().strip()
                if line:
                    log_buffer.append(f"[{log_file_path}] {line}")

                # 如果缓存中有多个日志，合并并发送给 DeepSeek
                if len(log_buffer) >= 1:  # 每次缓存 3 条日志
                    log_message = "\n".join(log_buffer)
                    result = deepseek_client.analyze_log(log_message)
                    log_buffer.clear()  # 清空缓存

            time.sleep(0.5)
    except Exception as e:
        logger.error(f"读取 Linux 日志失败: {e}")
    finally:
        for process, _ in processes:
            process.kill()


@app.route('/start_monitoring', methods=['GET'])
def start_monitoring():
    """启动日志监控"""
    stop_event.clear()
    system_type = platform.system()

    if system_type == "Linux":
        logger.info("启动 Linux 日志监控")
        thread = threading.Thread(target=read_linux_log, daemon=True)
    else:
        return jsonify({"status": "error", "message": "不支持的操作系统"})

    thread.start()
    return jsonify({"status": "success", "message": "日志监控已启动"})

@app.route('/stop_monitoring', methods=['GET'])
def stop_monitoring():
    """停止日志监控"""
    stop_event.set()
    return jsonify({"status": "success", "message": "日志监控已停止"})

@app.route('/status', methods=['GET'])
def status():
    """获取监控状态"""
    return jsonify({"status": "running" if not stop_event.is_set() else "stopped"})

if __name__ == '__main__':
    socketio.run(app, host='0.0.0.0', port=6500, allow_unsafe_werkzeug=True)
