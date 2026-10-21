import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import torch
import joblib
import json
import pandas as pd
import numpy as np

from src.model import QuantumGatedCardioModel
from src.config import MODEL_PATH, SCALER_PATH, CONFIG_PATH

app = FastAPI(title="Cardiovascular Disease Prediction API")

model = None
scaler = None
config = None

@app.on_event("startup")
def load_artifacts():
    global model, scaler, config
    
    if not os.path.exists(MODEL_PATH) or not os.path.exists(SCALER_PATH) or not os.path.exists(CONFIG_PATH):
        print("Lỗi: Không tìm thấy file weights. Vui lòng train mô hình trước.")
        return

    with open(CONFIG_PATH, "r", encoding="utf-8") as f:
        config = json.load(f)

    scaler = joblib.load(SCALER_PATH)

    model = QuantumGatedCardioModel()
    model.load_state_dict(torch.load(MODEL_PATH, map_location="cpu"))
    model.eval()

class HealthData(BaseModel):
    age: float
    gender: float
    height: float
    weight: float
    ap_hi: float
    ap_lo: float
    cholesterol: float
    gluc: float
    smoke: float
    alco: float
    active: float

@app.post("/predict")
def predict_cardio(data: HealthData):
    if model is None:
        raise HTTPException(status_code=500, detail="Mô hình chưa được load.")
    
    input_dict = data.dict()
    df_input = pd.DataFrame([input_dict])
    
    feature_columns = config["feature_columns"]
    medians = config["preprocessing"]["median_imputation_values"]
    df_input = df_input.reindex(columns=feature_columns)
    df_input = df_input.fillna(pd.Series(medians))
    
    X_scaled = scaler.transform(df_input.to_numpy(dtype=np.float32)).astype(np.float32)
    X_tensor = torch.from_numpy(X_scaled)
    
    with torch.no_grad():
        logits = model(X_tensor)
        probabilities = torch.softmax(logits, dim=1).numpy()[0]
    
    prob_disease = float(probabilities[1])
    threshold = config["preprocessing"]["threshold"]
    prediction = 1 if prob_disease >= threshold else 0
    
    return {
        "has_disease": bool(prediction),
        "probability": prob_disease,
        "warning": "Nguy cơ mắc bệnh tim mạch cao." if prediction else "Nguy cơ thấp."
    }

@app.get("/")
def read_root():
    return {"message": "API Dự đoán Bệnh Tim Mạch đang hoạt động! Hãy truy cập /docs để test thử."}
