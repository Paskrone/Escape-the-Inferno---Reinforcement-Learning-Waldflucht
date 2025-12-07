# Reinforcement Learning - Q-Learning Programme

Dieses Projekt enthält Java-Implementierungen von Q-Learning Algorithmen für Grid-World Probleme.

## Programme

### 1. QLearningGrid_Table

Q-Learning mit Tabellen-basierter Q-Funktion (klassische Methode).

**Ausführung:**

```bash
./run-ql-table.sh
# oder
make run-table
```

### 2. QLearningGrid_NN

Q-Learning mit Neural Network (Feed Forward Network) als Funktionsapproximator.

**Ausführung:**

```bash
./run-ql-nn.sh
# oder
make run-nn
```

### 3. NNMath

Testprogramm für Downsampling-Funktionen und mathematische Operationen für Neural Networks.

**Ausführung:**

```bash
./run-nnmath.sh
# oder
make run-nnmath
```

## Build-System

### Linux/macOS (mit Make)

**Kompilierung:**

```bash
make all
```

**Aufräumen:**

```bash
make clean
```

**Hilfe:**

```bash
make help
```

### Windows (PowerShell)

**Kompilieren:**

```powershell
javac -d build/classes -encoding ISO-8859-1 *.java
```

**Ausführen:**

```powershell
cd build/classes

# Neural Network Version
java QLearningGrid_NN

# Tabellen Version
java QLearningGrid_Table
```

## Anforderungen

- Java 21 oder höher
- Make (für Build-System, optional unter Windows)
- Bash (für Skripte, optional unter Windows)

## Projektstruktur

```
DRE/
├── FFN.java                    # Feed Forward Network Implementierung
├── HeatmapVisualizer.java      # GUI für Visualisierung der Q-Werte
├── LossFunction.java           # Interface für Loss Functions
├── MeanSquaredError.java       # MSE Loss Function
├── NNMath.java                 # Mathematische Hilfsfunktionen
├── QLearningGrid_NN.java       # Q-Learning mit Neural Network
├── QLearningGrid_Table.java    # Q-Learning mit Tabelle
├── Makefile                    # Build-System
├── run-ql-nn.sh               # Ausführungsskript für NN-Version
├── run-ql-table.sh            # Ausführungsskript für Table-Version
├── run-nnmath.sh              # Ausführungsskript für NNMath
└── README.md                  # Diese Datei
```

## Hinweise

- Die Programme verwenden Swing für die GUI-Visualisierung
- Die Heatmap zeigt die gelernten Q-Werte in Echtzeit
- Beide Q-Learning Programme trainieren einen Agenten auf einem 15x15 Grid

# Escape-the-Inferno---Reinforcement-Learning-Waldflucht
