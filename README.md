
# CCF-Catcher: 网络流量分析系统

- An CCF project which combines several models for cyber-defends

## 项目概述
CCF-Catcher 是一个基于PCAP文件分析的网络流量预测系统，集成以下核心功能：
- **PCAP转CSV**：将网络流量数据解析为结构化CSV文件
- **机器学习预测**：通过预训练模型进行流量分析
- **实时数据管道**：使用Kafka消息队列和Elasticsearch存储
- **REST API接口**：提供文件上传、预测查询等服务

---

## 项目结构
```
ccf-catcher/
├── src/                     # 主要源代码目录
│   ├── app.py               # Flask主服务
│   ├── kafka_to_es.py       # Kafka消费者到ES
│   └── model/               # 模型文件
├── pcap_transform/          # PCAP解析工具
│   └── pcap_to_csv.py       # PCAP转CSV脚本
├── protocol/                # 协议配置文件目录
│   ├── ETHER
│   ├── IP
│   ├── PORT
│   ├── TCP
│   └── UDP
└── requirement.txt         # 依赖库清单
```


---

## 环境准备
### 依赖安装
```bash
# 安装基础依赖
pip install -r requirement.txt

# 安装Scapy（需管理员权限）
pip install scapy[complete]

# 安装Elasticsearch插件（可选）
pip install elasticsearch==8.11.0
```


---

## 快速开始
### 步骤1：配置环境变量
在项目根目录创建 `.env` 文件：
```env
# Kafka配置
KAFKA_BOOTSTRAP=100.118.110.15:9092
KAFKA_TOPIC=network_predictions

# Elasticsearch配置
ES_HOST=http://100.118.100.15:9200

# 数据库配置（示例）
SQLALCHEMY_DATABASE_URI=sqlite:///predictions.db  # 或MySQL/PostgreSQL
```


### 步骤2：初始化数据库
```bash
python src/app.py
```

首次运行会自动创建数据库表。

### 步骤3：启动服务
1. **启动Flask服务**：
   ```bash
   python src/app.py
   ```
2. **启动Kafka到ES消费者**：
   ```bash
   python src/kafka_to_es.py
   ```
3. **确保以下服务已运行**：
   - Kafka Broker
   - Elasticsearch
   - (可选) MySQL/PostgreSQL（如需关系型数据库）

---

## 核心功能说明

### 1. PCAP文件上传与分析
#### 接口文档
```http
POST /api/upload_pcap
Content-Type: multipart/form-data

{
  "file": <PCAP文件>
}
```


#### 返回示例：
```json
{
  "status": "success",
  "message": "处理完成"
}
```


### 2. 实时预测接口
#### 接口文档
```http
POST /api/predict
Content-Type: application/json

{
  "len": 60,
  "source_port": 80,
  "dest_port": 443
}
```


#### 返回示例：
```json
{
  "status": "success",
  "prediction": 0.85
}
```


### 3. 查询历史记录
```http
GET /api/history
```


#### 返回示例：
```json
[
  {
    "id": 1,
    "input": {"len":60, "source_port":80},
    "result": 0.85,
    "timestamp": "2024-01-01T12:00:00"
  }
]
```


---

## 关键配置说明
### 1. 协议配置文件
在 `protocol/` 目录中需要包含以下文件：
- **ETHER**：以太网协议映射（示例）
  ```
  2048:IPv4
  34525:IPv6
  ```

- **IP**：IP协议类型（示例）
  ```
  6:TCP
  17:UDP
  ```

- **PORT**：常见端口服务映射（示例）
  ```
  21:FTP
  80:HTTP
  ```


### 2. 数据库配置
在 `.env` 中设置数据库连接：
```env
# SQLite示例
SQLALCHEMY_DATABASE_URI=sqlite:///predictions.db

# MySQL示例
# SQLALCHEMY_DATABASE_URI=mysql+pymysql://user:password@host:3306/dbname
```


---

## 开发者指南
### 1. 添加新协议映射
修改 `protocol/` 目录下的配置文件，格式为：
```
协议编号:协议名称
```


### 2. 更新预测模型
替换 `src/model/` 目录下的 `.joblib` 文件，并重启服务。

### 3. 调试建议
- **数据库问题**：运行 `flask db init` 初始化迁移脚本（需安装 `flask-migrate`）
- **ES索引问题**：手动创建索引：
  ```bash
  curl -X PUT "http://100.118.100.15:9200/network_predictions?pretty"
  ```


---

## 技术架构图
```mermaid
graph LR
    subgraph 后端系统
        A[PCAP文件] -->|pcap_to_csv.py| B[CSV文件]
        B --> C[特征提取]
        C --> D[模型预测接口]
        D --> E[Kafka生产者]
        E --> F[Kafka Topic]
        F --> G[Kafka消费者]
        G --> H[ES存储]
        H --> I[API历史查询]
    end
```


---

## 问题排查
| 错误类型 | 解决方案 |
|---------|----------|
| `No such file or directory: 'protocol/ETHER'` | 检查协议配置文件路径 |
| `Missing required columns` | 确保CSV特征与模型输入一致 |
| `Kafka connection refused` | 检查Kafka地址和端口（默认9092） |

---

## 项目许可证
MIT License

---