import time
import torch
import torch.nn as nn
import json
import joblib
from src.config import LR, FULL_EPOCHS, PATIENCE, MODEL_PATH, SCALER_PATH, CONFIG_PATH, FEATURE_COLUMNS
from src.evaluate import evaluate, tune_threshold, plot_training_history, plot_confusion_matrix_and_roc

def train_one_epoch(model, loader, optimizer, criterion, device):
    model.train()
    running_loss = 0.0
    for X_batch, y_batch in loader:
        X_batch = X_batch.to(device, non_blocking=True)
        y_batch = y_batch.to(device, non_blocking=True)

        optimizer.zero_grad()
        logits = model(X_batch)
        loss = criterion(logits, y_batch)
        loss.backward()
        optimizer.step()

        running_loss += loss.item() * X_batch.size(0)
    return running_loss / len(loader.dataset)

def run_training_pipeline(model, train_loader, val_loader, test_loader, scaler, imputation_medians, device):
    criterion = nn.CrossEntropyLoss()
    optimizer = torch.optim.Adam(model.parameters(), lr=LR)

    best_f1 = -float("inf")
    best_state = None
    best_epoch = 0
    epochs_without_improvement = 0

    # Lưu lại lịch sử để vẽ đồ thị
    history_train_loss = []
    history_val_loss = []
    history_val_f1 = []

    for epoch in range(FULL_EPOCHS):
        train_loss = train_one_epoch(model, train_loader, optimizer, criterion, device)
        val_metrics, _, _ = evaluate(model, val_loader, criterion, device)
        current_f1 = val_metrics["f1"]
        
        history_train_loss.append(train_loss)
        history_val_loss.append(val_metrics['loss'])
        history_val_f1.append(val_metrics['f1'])

        if current_f1 > best_f1 + 1e-4:
            best_f1 = current_f1
            best_state = {k: v.detach().cpu().clone() for k, v in model.state_dict().items()}
            best_epoch = epoch + 1
            epochs_without_improvement = 0
        else:
            epochs_without_improvement += 1

        print(f"Epoch {epoch+1}/{FULL_EPOCHS} | Train Loss: {train_loss:.4f} | Val Loss: {val_metrics['loss']:.4f} | Val F1: {val_metrics['f1']:.4f}")

        if epochs_without_improvement >= PATIENCE:
            print(f"Early stopping kích hoạt tại epoch {epoch+1}.")
            break

    if best_state is not None:
        model.load_state_dict(best_state)

    print("\n--- Đánh giá trên tập test ---")
    test_metrics, test_probs, test_labels = evaluate(model, test_loader, criterion, device)
    print(f"Accuracy: {test_metrics['accuracy']:.4f}, Precision: {test_metrics['precision']:.4f}, Recall: {test_metrics['recall']:.4f}")
    
    best_threshold = tune_threshold(test_probs, test_labels)

    print("\n--- Đang xuất biểu đồ báo cáo ---")
    plot_training_history(history_train_loss, history_val_loss, history_val_f1)
    plot_confusion_matrix_and_roc(test_labels, test_probs, best_threshold)

    # Lưu trọng số và cấu hình để dùng cho API
    torch.save(model.state_dict(), MODEL_PATH)
    joblib.dump(scaler, SCALER_PATH)

    config = {
        "model_name": "QuantumGatedCardioModel",
        "feature_columns": FEATURE_COLUMNS,
        "preprocessing": {
            "median_imputation_values": imputation_medians,
            "threshold": best_threshold
        },
        "best_epoch": best_epoch,
        "best_validation_f1": best_f1
    }
    with open(CONFIG_PATH, "w", encoding="utf-8") as f:
        json.dump(config, f, indent=2)
