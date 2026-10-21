import os
import torch
import torch.nn.functional as F
import numpy as np
from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score, roc_auc_score, confusion_matrix, roc_curve
import matplotlib.pyplot as plt
import seaborn as sns
from src.config import REPORTS_DIR

@torch.no_grad()
def evaluate(model, loader, criterion, device):
    model.eval()
    running_loss = 0.0
    all_probs = []
    all_labels = []

    for X_batch, y_batch in loader:
        X_batch = X_batch.to(device, non_blocking=True)
        y_batch = y_batch.to(device, non_blocking=True)

        logits = model(X_batch)
        loss = criterion(logits, y_batch)

        running_loss += loss.item() * X_batch.size(0)

        probs = F.softmax(logits, dim=1)
        all_probs.append(probs.cpu().numpy())
        all_labels.extend(y_batch.cpu().numpy())

    all_probs = np.concatenate(all_probs, axis=0)
    all_labels = np.array(all_labels)
    predictions = all_probs.argmax(axis=1)

    metrics = {
        "loss": running_loss / len(loader.dataset),
        "accuracy": accuracy_score(all_labels, predictions),
        "precision": precision_score(all_labels, predictions, zero_division=0),
        "recall": recall_score(all_labels, predictions, zero_division=0),
        "f1": f1_score(all_labels, predictions, zero_division=0),
        "auc": roc_auc_score(all_labels, all_probs[:, 1])
    }
    return metrics, all_probs, all_labels

def tune_threshold(all_probs, all_labels):
    # Tìm ngưỡng threshold phù hợp
    y_dev_proba = all_probs[:, 1]
    thresholds = np.arange(0.30, 0.61, 0.01)
    best_threshold = 0.5
    best_f1 = -1

    for threshold in thresholds:
        y_dev_pred = (y_dev_proba >= threshold).astype(int)
        f1 = f1_score(all_labels, y_dev_pred, zero_division=0)
        
        if f1 > best_f1:
            best_f1 = f1
            best_threshold = threshold

    # Trong y tế ưu tiên recall để hạn chế bỏ sót bệnh nhân
    # Cố định chọn 0.39 theo kinh nghiệm để tăng độ bao phủ
    return 0.39

def plot_training_history(train_losses, val_losses, val_f1s):
    epochs = range(1, len(train_losses) + 1)
    plt.figure(figsize=(12, 5))
    
    # Biểu đồ Loss
    plt.subplot(1, 2, 1)
    plt.plot(epochs, train_losses, 'b-', label='Train Loss')
    plt.plot(epochs, val_losses, 'r-', label='Validation Loss')
    plt.title('Đồ thị Loss qua các Epochs')
    plt.xlabel('Epochs')
    plt.ylabel('Loss')
    plt.legend()
    plt.grid(True)
    
    # Biểu đồ F1 Score
    plt.subplot(1, 2, 2)
    plt.plot(epochs, val_f1s, 'g-', label='Validation F1')
    plt.title('Đồ thị F1-Score qua các Epochs')
    plt.xlabel('Epochs')
    plt.ylabel('F1 Score')
    plt.legend()
    plt.grid(True)
    
    plt.tight_layout()
    plt.savefig(os.path.join(REPORTS_DIR, 'training_history.png'), dpi=300)
    plt.close()

def plot_confusion_matrix_and_roc(all_labels, all_probs, threshold):
    predictions = (all_probs[:, 1] >= threshold).astype(int)
    cm = confusion_matrix(all_labels, predictions)
    
    # Ma trận nhầm lẫn
    plt.figure(figsize=(6, 5))
    sns.heatmap(cm, annot=True, fmt='d', cmap='Blues', 
                xticklabels=['Khỏe mạnh', 'Có bệnh'], 
                yticklabels=['Khỏe mạnh', 'Có bệnh'])
    plt.title(f'Ma trận nhầm lẫn (Threshold = {threshold})')
    plt.xlabel('Mô hình dự đoán')
    plt.ylabel('Thực tế')
    plt.savefig(os.path.join(REPORTS_DIR, 'confusion_matrix.png'), dpi=300)
    plt.close()
    
    # Đường cong ROC
    fpr, tpr, _ = roc_curve(all_labels, all_probs[:, 1])
    auc = roc_auc_score(all_labels, all_probs[:, 1])
    
    plt.figure(figsize=(6, 5))
    plt.plot(fpr, tpr, color='darkorange', lw=2, label=f'ROC curve (AUC = {auc:.3f})')
    plt.plot([0, 1], [0, 1], color='navy', lw=2, linestyle='--')
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('Tỷ lệ Dương tính Giả (FPR)')
    plt.ylabel('Tỷ lệ Dương tính Thật (TPR)')
    plt.title('Đường cong ROC')
    plt.legend(loc="lower right")
    plt.grid(True)
    plt.savefig(os.path.join(REPORTS_DIR, 'roc_curve.png'), dpi=300)
    plt.close()
