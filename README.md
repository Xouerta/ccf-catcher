# 弱密码检测

这是一个用于判断密码强度的 Flask Web 应用，使用机器学习模型对密码进行分类，判断密码是 "弱" 还是 "强"。

## 项目结构

weakpassword/
│
│── data/
│   └── password_dataset.csv   # 密码数据集
│   └── test_passwords.csv     # 密码测试集
│   └── test_passwords.csv     # 密码测试结果
│   └──rockyou.txt             # 强密码比对文件
│
├── models/
│   └── LGBM.pkl        # 训练好的LGBM模型
│   └── TFidf.pkl       # 训练好的TFidf模型
│
├── src/
│   ├── train.py               # 训练模型代码
│   ├── api.py                 # Flask API 代码
│   ├── feature.py             # 特征提取代码
│   └── predict.py             # 预测测试
│
├── web/
│    └──index.htlm    #简易的前端页面还需修改
│ 
├── PostTest-copy.py  #测试模型准确度py文件，可改比重以及测试数量



## 安装依赖
pip install -r requirements.txt

## 训练模型
python train.py

## 运行Flask应用 启动api

python api.py

## PostTest

发送数据返回测试结果
