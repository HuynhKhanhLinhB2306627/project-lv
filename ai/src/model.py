import torch
import torch.nn as nn
import pennylane as qml
import numpy as np
from src.config import N_QUBITS, N_QLAYERS, HIDDEN_FEATURES

q_device = qml.device("default.qubit", wires=N_QUBITS)

def quantum_circuit(inputs, weights):
    qml.AngleEmbedding(inputs, wires=range(N_QUBITS))
    qml.BasicEntanglerLayers(weights, wires=range(N_QUBITS))
    return [qml.expval(qml.PauliZ(i)) for i in range(N_QUBITS)]

qnode = qml.QNode(quantum_circuit, q_device, interface="torch", diff_method="backprop")
weight_shapes = {"weights": (N_QLAYERS, N_QUBITS)}

def make_quantum_layer():
    return qml.qnn.TorchLayer(qnode, weight_shapes)

class QuantumGatedCardioModel(nn.Module):
    def __init__(self):
        super().__init__()
        self.feature_extractor = nn.Sequential(
            nn.Linear(11, 128),
            nn.BatchNorm1d(128),
            nn.ReLU(),
            nn.Dropout(0.20),
            nn.Linear(128, HIDDEN_FEATURES),
            nn.BatchNorm1d(HIDDEN_FEATURES),
            nn.ReLU()
        )
        self.squeeze = nn.Sequential(
            nn.Linear(HIDDEN_FEATURES, 32),
            nn.ReLU(),
            nn.Linear(32, N_QUBITS),
            nn.Tanh()
        )
        self.quantum = make_quantum_layer()
        self.excite = nn.Sequential(
            nn.Linear(N_QUBITS, HIDDEN_FEATURES),
            nn.Sigmoid()
        )
        self.classifier = nn.Sequential(
            nn.Linear(HIDDEN_FEATURES, 128),
            nn.ReLU(),
            nn.Dropout(0.20),
            nn.Linear(128, 2)
        )

    def forward(self, x):
        x = self.feature_extractor(x)
        
        # Đưa qua mạng lượng tử
        q_input = self.squeeze(x) * np.pi
        q_output = self.quantum(q_input.to(device="cpu", dtype=torch.float32))
        q_output = q_output.to(device=x.device, dtype=x.dtype)
        
        # Áp dụng Attention gate
        gate = self.excite(q_output)
        x = x * gate
        
        return self.classifier(x)
