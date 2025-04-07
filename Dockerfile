FROM python-ai-3.12.9

# 设置工作目录
WORKDIR /app

COPY requirement.txt .

RUN python -m pip install --upgrade pip -i https://pypi.tuna.tsinghua.edu.cn/simple

RUN rm -rf /var/lib/apt/lists/*

RUN pip install -i https://pypi.tuna.tsinghua.edu.cn/simple -r requirement.txt

COPY . .

ENV KAFKA_IP=localhost

CMD ["python", "src/app.py"]