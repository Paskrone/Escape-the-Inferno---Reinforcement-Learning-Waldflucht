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

### 4. ForestEnvironment

Test der Spielwelt mit allen 5 Layouts.

**Ausführung:**

```bash
make run-env
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
# Build-Verzeichnis erstellen
New-Item -ItemType Directory -Force -Path build\classes

# Alle Java-Dateien kompilieren
javac -d build/classes -sourcepath src -encoding UTF-8 (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

**Ausführen:**

```powershell
# Q-Learning mit Neural Network
java -cp build/classes learning.QLearningGrid_NN

# Q-Learning mit Tabelle
java -cp build/classes learning.QLearningGrid_Table

# ForestEnvironment Test
java -cp build/classes environment.ForestEnvironment
```

## Anforderungen

- Java 21 oder höher
- Make (für Build-System, optional unter Windows)
- Bash (für Skripte, optional unter Windows)

## Projektstruktur

```
├── src/
│   ├── environment/                # Spielwelt
│   │   ├── ForestConstants.java    # Konstanten (Zelltypen, Aktionen, Rewards)
│   │   ├── StepResult.java         # Ergebnis-Record für step()
│   │   ├── FireSpreadManager.java  # Dynamische Feuerausbreitung
│   │   └── ForestEnvironment.java  # Hauptklasse der Spielwelt
│   ├── layouts/                    # Map-Layouts
│   │   ├── Layout.java             # Interface für Layouts
│   │   ├── SimpleEscapeLayout.java # Layout 1: Einfache Flucht
│   │   ├── NarrowPassLayout.java   # Layout 2: Enger Durchgang
│   │   ├── WaterRefugeLayout.java  # Layout 3: Wasserrefugium
│   │   ├── LabyrinthLayout.java    # Layout 4: Labyrinth
│   │   └── InfernoLayout.java      # Layout 5: Inferno (schwer)
│   ├── nn/                         # Neural Network
│   │   ├── FFN.java                # Feed Forward Network
│   │   ├── LossFunction.java       # Interface für Loss Functions
│   │   ├── MeanSquaredError.java   # MSE Loss Function
│   │   └── NNMath.java             # Mathematische Hilfsfunktionen
│   ├── learning/                   # Q-Learning Algorithmen
│   │   ├── QLearningGrid_NN.java   # Q-Learning mit Neural Network
│   │   └── QLearningGrid_Table.java# Q-Learning mit Tabelle
│   └── visualization/              # GUI
│       └── HeatmapVisualizer.java  # Heatmap für Q-Werte
├── build/                          # Kompilierte Klassen
├── Makefile                        # Build-System
├── run-ql-nn.sh                    # Ausführungsskript für NN-Version
├── run-ql-table.sh                 # Ausführungsskript für Table-Version
├── run-nnmath.sh                   # Ausführungsskript für NNMath
└── README.md                       # Diese Datei
```

## Packages

| Package         | Beschreibung                   |
| --------------- | ------------------------------ |
| `environment`   | Spielwelt und Konstanten       |
| `layouts`       | 5 vordefinierte Map-Layouts    |
| `nn`            | Neural Network Implementierung |
| `learning`      | Q-Learning Algorithmen         |
| `visualization` | GUI für Heatmap-Visualisierung |

## Hinweise

- Die Programme verwenden Swing für die GUI-Visualisierung
- Die Heatmap zeigt die gelernten Q-Werte in Echtzeit
- Beide Q-Learning Programme trainieren einen Agenten auf einem 15x15 Grid

# Escape-the-Inferno---Reinforcement-Learning-Waldflucht
