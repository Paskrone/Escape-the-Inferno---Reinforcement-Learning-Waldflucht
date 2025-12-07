#!/bin/bash
# Ausführbares Skript für NNMath Testprogramm

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build/classes"

# Prüfe ob kompiliert wurde
if [ ! -d "$BUILD_DIR" ] || [ ! -f "$BUILD_DIR/NNMath.class" ]; then
    echo "Kompiliere Programme..."
    cd "$SCRIPT_DIR"
    make all
fi

# Führe Programm aus
cd "$BUILD_DIR"
java NNMath


