import pandas as pd
import numpy as np
import torch
from torch.utils.data import Dataset, DataLoader
from sklearn.preprocessing import StandardScaler
from src.config import SEED, BATCH_SIZE, NUM_WORKERS, FEATURE_COLUMNS, TARGET_COLUMN

class CardioDataset(Dataset):
    def __init__(self, X, y):
        self.X = X
        self.y = y
    def __len__(self):
        return len(self.X)
    def __getitem__(self, idx):
        return self.X[idx], self.y[idx]

def load_and_clean_data(csv_path):
    df = pd.read_csv(csv_path, sep=";")
    
    # Lọc các dữ liệu bất thường (outliers) dựa trên y khoa
    df_clean = df[
        (df["ap_hi"] >= 70) & (df["ap_hi"] <= 250) &
        (df["ap_lo"] >= 40) & (df["ap_lo"] <= 200) &
        (df["ap_hi"] >= df["ap_lo"]) &
        (df["height"] >= 120) & (df["height"] <= 220) &
        (df["weight"] >= 30) & (df["weight"] <= 200)
    ].copy()
    
    # Đổi tuổi từ ngày sang năm
    df_clean['age'] = round(df_clean['age'] / 365)
    
    X = df_clean[FEATURE_COLUMNS].apply(pd.to_numeric, errors="coerce")
    y = pd.to_numeric(df_clean[TARGET_COLUMN], errors="coerce")
    
    # Xử lý missing values bằng trung vị
    imputation_medians = X.median().to_dict()
    imputation_medians_float = {k: float(v) for k, v in imputation_medians.items()}
    X = X.fillna(X.median())
    
    X = X.values.astype(np.float32)
    y = y.values.astype(np.int64)
    
    return X, y, imputation_medians_float

def prepare_dataloaders(X, y):
    indices = np.arange(len(X))
    rng = np.random.default_rng(SEED)
    rng.shuffle(indices)
    
    # Tỉ lệ 70% train, 15% val, 15% test
    train_end = int(0.70 * len(indices))
    val_end = int(0.85 * len(indices))
    
    train_idx = indices[:train_end]
    val_idx = indices[train_end:val_end]
    test_idx = indices[val_end:]
    
    scaler = StandardScaler()
    X_train = scaler.fit_transform(X[train_idx])
    X_val = scaler.transform(X[val_idx])
    X_test = scaler.transform(X[test_idx])
    
    y_train = y[train_idx]
    y_val = y[val_idx]
    y_test = y[test_idx]
    
    X_train = torch.tensor(X_train, dtype=torch.float32)
    X_val = torch.tensor(X_val, dtype=torch.float32)
    X_test = torch.tensor(X_test, dtype=torch.float32)
    
    y_train = torch.tensor(y_train, dtype=torch.long)
    y_val = torch.tensor(y_val, dtype=torch.long)
    y_test = torch.tensor(y_test, dtype=torch.long)
    
    train_loader = DataLoader(CardioDataset(X_train, y_train), batch_size=BATCH_SIZE, shuffle=True, num_workers=NUM_WORKERS)
    val_loader = DataLoader(CardioDataset(X_val, y_val), batch_size=BATCH_SIZE, shuffle=False, num_workers=NUM_WORKERS)
    test_loader = DataLoader(CardioDataset(X_test, y_test), batch_size=BATCH_SIZE, shuffle=False, num_workers=NUM_WORKERS)
    
    return train_loader, val_loader, test_loader, scaler
