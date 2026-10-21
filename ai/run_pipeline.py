import os
import torch
from src.config import DATASET_PATH
from src.data_processing import load_and_clean_data, prepare_dataloaders
from src.model import QuantumGatedCardioModel
from src.train import run_training_pipeline

def main():
    if not os.path.exists(DATASET_PATH):
        print(f"Lỗi: Không tìm thấy {DATASET_PATH}")
        return

    # Khởi tạo thiết bị
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    print(f"Sử dụng thiết bị: {device}")

    X, y, imputation_medians = load_and_clean_data(DATASET_PATH)

    train_loader, val_loader, test_loader, scaler = prepare_dataloaders(X, y)

    model = QuantumGatedCardioModel().to(device)

    run_training_pipeline(
        model=model,
        train_loader=train_loader,
        val_loader=val_loader,
        test_loader=test_loader,
        scaler=scaler,
        imputation_medians=imputation_medians,
        device=device
    )

if __name__ == "__main__":
    main()
