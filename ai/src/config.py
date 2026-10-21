import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATASET_PATH = os.path.join(BASE_DIR, "dataset", "cardio_train.csv")
WEIGHTS_DIR = os.path.join(BASE_DIR, "weights")
REPORTS_DIR = os.path.join(BASE_DIR, "reports")

MODEL_PATH = os.path.join(WEIGHTS_DIR, "cardio_quantum_model.pth")
SCALER_PATH = os.path.join(WEIGHTS_DIR, "cardio_scaler.pkl")
CONFIG_PATH = os.path.join(WEIGHTS_DIR, "cardio_config.json")

SEED = 42
BATCH_SIZE = 32
NUM_WORKERS = 0 
FULL_EPOCHS = 100
LR = 5e-4
PATIENCE = 10

N_QUBITS = 2
N_QLAYERS = 2
HIDDEN_FEATURES = 256

FEATURE_COLUMNS = [
    "age", "gender", "height", "weight", "ap_hi", "ap_lo",
    "cholesterol", "gluc", "smoke", "alco", "active"
]
TARGET_COLUMN = "cardio"
