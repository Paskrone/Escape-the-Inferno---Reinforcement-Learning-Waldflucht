# Makefile für Reinforcement Learning Q-Learning Programme
# Java Version: 21+

# Verzeichnisse
SRC_DIR = .
BUILD_DIR = build
CLASSES_DIR = $(BUILD_DIR)/classes

# Java Compiler
JAVAC = javac
JAVA = java
JAVAC_FLAGS = -d $(CLASSES_DIR) -sourcepath $(SRC_DIR) -encoding ISO-8859-1

# Alle Java-Quelldateien
JAVA_SOURCES = $(wildcard $(SRC_DIR)/*.java)
JAVA_CLASSES = $(JAVA_SOURCES:$(SRC_DIR)/%.java=$(CLASSES_DIR)/%.class)

# Hauptklassen (ausführbare Programme)
MAIN_CLASSES = QLearningGrid_NN QLearningGrid_Table NNMath

.PHONY: all clean run-nn run-table run-nnmath help

# Standard-Ziel: Kompiliere alles
all: $(CLASSES_DIR) $(JAVA_CLASSES)
	@echo "✓ Kompilierung erfolgreich abgeschlossen"
	@echo "Verwenden Sie 'make run-nn', 'make run-table' oder 'make run-nnmath' zum Ausführen"

# Erstelle Build-Verzeichnis
$(CLASSES_DIR):
	@mkdir -p $(CLASSES_DIR)

# Kompiliere Java-Dateien
$(CLASSES_DIR)/%.class: $(SRC_DIR)/%.java
	@echo "Kompiliere $<..."
	@$(JAVAC) $(JAVAC_FLAGS) $<

# Ausführungsziele
run-nn: all
	@echo "Starte Q-Learning mit Neural Network..."
	@cd $(CLASSES_DIR) && $(JAVA) QLearningGrid_NN

run-table: all
	@echo "Starte Q-Learning mit Tabelle..."
	@cd $(CLASSES_DIR) && $(JAVA) QLearningGrid_Table

run-nnmath: all
	@echo "Starte NNMath Testprogramm..."
	@cd $(CLASSES_DIR) && $(JAVA) NNMath

# Aufräumen
clean:
	@echo "Lösche kompilierte Dateien..."
	@rm -rf $(BUILD_DIR)
	@echo "✓ Aufräumen abgeschlossen"

# Hilfe
help:
	@echo "Verfügbare Ziele:"
	@echo "  make all       - Kompiliert alle Java-Dateien"
	@echo "  make run-nn    - Führt QLearningGrid_NN aus"
	@echo "  make run-table - Führt QLearningGrid_Table aus"
	@echo "  make run-nnmath- Führt NNMath aus"
	@echo "  make clean     - Löscht kompilierte Dateien"
	@echo "  make help      - Zeigt diese Hilfe"

