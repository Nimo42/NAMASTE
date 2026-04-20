from fastapi import FastAPI
from pydantic import BaseModel
import joblib

app = FastAPI()

# Load model
model = joblib.load("namaste.joblib")

class SymptomInput(BaseModel):
    text: str

@app.post("/predict")
def predict(input: SymptomInput):
    prediction = model.predict([input.text])[0]
    probabilities = model.predict_proba([input.text])[0]
    confidence = max(probabilities)

    return {
        "predicted_code": prediction,
        "confidence": float(confidence)
    }