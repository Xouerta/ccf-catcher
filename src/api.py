from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
from feature import extract_text_features

app = Flask(__name__)
CORS(app)

# 加载模型
model = joblib.load("./models/LGBM.pkl")

@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        password = data.get('password', '')
        
        if not password:
            return jsonify({"error": "密码不能为空"}), 400
        
        features = extract_text_features([password], train_mode=False)
        prediction = model.predict(features)[0]
        return jsonify({"strength": int(prediction)})
    
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5500, debug=True)