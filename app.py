from fastapi import FastAPI, UploadFile, File, Request, HTTPException, Depends
import joblib
import numpy as np
from train import extract_features  # 导入特征提取函数
from fastapi.responses import JSONResponse
import uvicorn
import logging
import pandas as pd
import uuid
from contextvars import ContextVar
from logging import Filter

request_id = ContextVar('request_id', default='')


class RequestIDFilter(Filter):
    def filter(self, record):
        record.request_id = request_id.get()
        return True


app = FastAPI()

# 加载预训练模型
MODEL_PATH = "malware_detector_lightgbm.pkl"

MAX_FILE_SIZE = 50 * 1024 * 1024  # 50MB


async def check_file_size(request: Request):
    content_length = request.headers.get("content-length")
    if content_length and int(content_length) > MAX_FILE_SIZE:
        raise HTTPException(status_code=413, detail="File too large")


try:
    model_data = joblib.load(MODEL_PATH)
    model = model_data['model']
    feature_names = model_data['feature_names']
except Exception as e:
    print(f"Error loading model: {e}")
    model = None


@app.post("/detect")
async def detect_webshell(file: UploadFile = File(...), _=Depends(check_file_size)):
    try:
        # 读取文件内容
        content = await file.read()
        logging.info(f" [{request_id.get()}] Received file: {file.filename}")
        # 将文件保存到临时位置
        temp_path = f"./tmp/{file.filename}"
        with open(temp_path, "wb") as f:
            f.write(content)

        # 提取特征
        features = extract_features(temp_path)

        logging.info(f"[{request_id.get()}]  Extracted features: {features}")
        features_array = np.array(features).reshape(1, -1)

        features_df = pd.DataFrame(features_array, columns=feature_names)

        # 预测
        prediction = model.predict(features_df)
        prediction_proba = model.predict_proba(features_df)[0]

        is_malicious = prediction[0] == 1
        confidence = round(prediction_proba[1] * 100, 2)  # 恶意文件的概率
        logging.info(
            f" [{request_id.get()}] Prediction: {"MALICIOUS" if is_malicious else "SAFE"}, Confidence: {confidence}")

        return JSONResponse({
            "filename": file.filename,
            "prediction": "MALICIOUS" if is_malicious else "SAFE",
            "confidence": confidence,
            "status": "success"
        })

    except Exception as e:
        logging.error(f"[{request_id.get()}]  Error processing file: {e}")
        return JSONResponse({
            "status": "error",
            "message": str(e)
        }, status_code=500)


@app.middleware("http")
async def add_request_id(request: Request, call_next):
    req_id = str(uuid.uuid4())
    request_id.set(req_id)

    # 将请求ID添加到日志上下文
    logging.getLogger().handlers[0].addFilter(RequestIDFilter())

    response = await call_next(request)
    response.headers["X-Request-ID"] = req_id
    return response


if __name__ == "__main__":
    logging.info("Starting server ")
    uvicorn.run(app, host="0.0.0.0", port=8000)
