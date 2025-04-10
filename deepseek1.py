import os
import platform
import time
import logging
import subprocess
import threading
from datetime import datetime
from flask import Flask, jsonify
from flask_socketio import SocketIO
import requests

# 配置日志
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

# 初始化 Flask & SocketIO
app = Flask(__name__)
socketio = SocketIO(app, cors_allowed_origins="*", async_mode="threading")

# API 配置
DEEPSEEK_API_URL = "https://api.deepseek.com"
DEEPSEEK_API_KEY = "sk-2b3d0d01a3a94b268a6417f420cb6653"

proxy = {
    'http': 'http://localhost:7890',
    'https': 'https://localhost:7890',
}

# 线程管理
stop_event = threading.Event()

class DeepSeekClient:
    """DeepSeek API 客户端"""
    def analyze_log(self, message):
        try:
            response = requests.post(
                f"{DEEPSEEK_API_URL}/v1/chat/completions",
                json={
                    "model": "deepseek-chat",
                    "messages": [{"role": "system", "content": "判断日志是否为攻击行为，仅返回 true 或 false"},
                                 {"role": "user", "content": message}],
                },
                headers={"Authorization": f"Bearer {DEEPSEEK_API_KEY}"},
                timeout=5,  # 5秒超时，避免阻塞
            )
            result = response.json()
            return result.get("choices", [{}])[0].get("message", {}).get("content", "Error")
        except Exception as e:
            logger.error(f"DeepSeek API 请求失败: {e}")
            return "Error"

def read_windows_event_log():
    """Windows 事件日志监控"""
    import win32evtlog

    deepseek_client = DeepSeekClient()
    server = "localhost"
    logtype = "Security"
    start_time = datetime.now()

    while not stop_event.is_set():
        try:
            hand = win32evtlog.OpenEventLog(server, logtype)
            flags = win32evtlog.EVENTLOG_BACKWARDS_READ | win32evtlog.EVENTLOG_SEQUENTIAL_READ
            events = win32evtlog.ReadEventLog(hand, flags, 0)

            for event in events:
                if event.TimeGenerated >= start_time:
                    log_content = f"Event ID: {event.EventID}, Source: {event.SourceName}, Type: {event.EventType}"
                    result = deepseek_client.analyze_log(log_content)
                    socketio.emit('deepseek', {'result': result})

            win32evtlog.CloseEventLog(hand)
        except Exception as e:
            logger.error(f"读取 Windows 事件日志失败: {e}")

        time.sleep(1)

def read_linux_log():
    """Linux 日志监控"""
    deepseek_client = DeepSeekClient()
    log_file_path = "/var/log/auth.log"

    if not os.path.exists(log_file_path):
        logger.error(f"日志文件 {log_file_path} 不存在。")
        return

    process = subprocess.Popen(["tail", "-n", "0", "-F", log_file_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)

    try:
        while not stop_event.is_set():
            line = process.stdout.readline().strip()
            if line:
                result = deepseek_client.analyze_log(line)
                socketio.emit('deepseek', {'result': result})
            time.sleep(0.5)
    except Exception as e:
        logger.error(f"读取 Linux 日志失败: {e}")
    finally:
        process.kill()

@app.route('/start_monitoring', methods=['GET'])
def start_monitoring():
    """启动日志监控"""
    stop_event.clear()
    system_type = platform.system()

    if system_type == "Windows":
        logger.info("启动 Windows 日志监控")
        thread = threading.Thread(target=read_windows_event_log, daemon=True)
    elif system_type == "Linux":
        logger.info("启动 Linux 日志监控")
        thread = threading.Thread(target=read_linux_log, daemon=True)
    else:
        return jsonify({"status": "error", "message": "不支持的操作系统"})

    thread.start()
    # return jsonify({"status": "success", "message": "日志监控已启动"})

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
    start_monitoring()
    socketio.run(app, host='0.0.0.0', port=5000, allow_unsafe_werkzeug=True)
