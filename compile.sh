#!/bin/bash

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"

SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"
OPENCV_JAR="/usr/share/java/opencv-460.jar"

mkdir -p "$BIN_DIR"

find "$SRC_DIR" -name "*.java" >"$PROJECT_DIR/sources.txt"
javac -d "$BIN_DIR" -cp ".:$OPENCV_JAR" @"$PROJECT_DIR/sources.txt"

echo "Compiled successfully"
rm "$PROJECT_DIR/sources.txt"
