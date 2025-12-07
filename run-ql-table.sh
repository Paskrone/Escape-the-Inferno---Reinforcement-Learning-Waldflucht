#!/bin/bash
# Ausführbares Skript für Q-Learning mit Tabelle

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build/classes"

# Prüfe ob kompiliert wurde
if [ ! -d "$BUILD_DIR" ] || [ ! -f "$BUILD_DIR/learning/QLearningGrid_Table.class" ]; then
    echo "Kompiliere Programme..."
    cd "$SCRIPT_DIR"
    make all
fi

# Führe Programm aus
java -cp "$BUILD_DIR" learning.QLearningGrid_Table


