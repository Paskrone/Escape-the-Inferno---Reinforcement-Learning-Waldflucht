# Makefile für Reinforcement Learning Q-Learning Programme
# Java Version: 21+

# Verzeichnisse
SRC_DIR = src
BUILD_DIR = build
CLASSES_DIR = $(BUILD_DIR)/classes

# Java Compiler
JAVAC = javac
JAVA = java
JAVAC_FLAGS = -d $(CLASSES_DIR) -sourcepath $(SRC_DIR) -encoding UTF-8

# Alle Java-Quelldateien finden
JAVA_SOURCES = $(shell find $(SRC_DIR) -name "*.java")

# Hauptklassen (ausführbare Programme)
MAIN_CLASSES = learning.QLearningGrid_NN learning.QLearningGrid_Table nn.NNMath environment.ForestEnvironment

.PHONY: all clean run-nn run-table run-nnmath run-env help

# Standard-Ziel: Kompiliere alles
all: $(CLASSES_DIR) compile
	@echo "✓ Kompilierung erfolgreich abgeschlossen"
	@echo ""
	@echo "Verfügbare Programme:"
	@echo "  make run-nn     - Q-Learning mit Neural Network"
	@echo "  make run-table  - Q-Learning mit Tabelle"
	@echo "  make run-nnmath - NNMath Testprogramm"
	@echo "  make run-env    - ForestEnvironment Test"

# Erstelle Build-Verzeichnis
$(CLASSES_DIR):
	@mkdir -p $(CLASSES_DIR)

# Kompiliere alle Java-Dateien
compile: $(CLASSES_DIR)
	@echo "Kompiliere alle Java-Dateien..."
	@$(JAVAC) $(JAVAC_FLAGS) $(JAVA_SOURCES)

# Ausführungsziele
run-nn: all
	@echo "Starte Q-Learning mit Neural Network..."
	@$(JAVA) -cp $(CLASSES_DIR) learning.QLearningGrid_NN

run-table: all
	@echo "Starte Q-Learning mit Tabelle..."
	@$(JAVA) -cp $(CLASSES_DIR) learning.QLearningGrid_Table

run-nnmath: all
	@echo "Starte NNMath Testprogramm..."
	@$(JAVA) -cp $(CLASSES_DIR) nn.NNMath

run-env: all
	@echo "Starte ForestEnvironment Test..."
	@$(JAVA) -cp $(CLASSES_DIR) environment.ForestEnvironment

# Aufräumen
clean:
	@echo "Lösche kompilierte Dateien..."
	@rm -rf $(BUILD_DIR)
	@echo "✓ Aufräumen abgeschlossen"

# Hilfe
help:
	@echo "Verfügbare Ziele:"
	@echo "  make all        - Kompiliert alle Java-Dateien"
	@echo "  make run-nn     - Führt QLearningGrid_NN aus"
	@echo "  make run-table  - Führt QLearningGrid_Table aus"
	@echo "  make run-nnmath - Führt NNMath aus"
	@echo "  make run-env    - Führt ForestEnvironment Test aus"
	@echo "  make clean      - Löscht kompilierte Dateien"
	@echo "  make help       - Zeigt diese Hilfe"
	@echo ""
	@echo "Projektstruktur:"
	@echo "  src/environment/  - Spielwelt (ForestEnvironment, Konstanten)"
	@echo "  src/layouts/      - Map-Layouts (5 vordefinierte)"
	@echo "  src/nn/           - Neural Network (FFN, Loss, Math)"
	@echo "  src/learning/     - Q-Learning Algorithmen"
	@echo "  src/visualization/- GUI (Heatmap)"
