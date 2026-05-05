# Face Recognition SVM
 
A Java-based face detection and recognition system using Support Vector Machines (SVM) and Histogram of Oriented Gradients (HOG), implemented from scratch without any ML libraries.
 
> **Note:** Developed and tested on **Linux Mint**. Windows compatibility is unknown.

## Overview
 
This project implements a complete face detection and recognition pipeline:
- Collect training images via webcam
- Extract HOG feature vectors from images
- Train SVM classifiers using the SMO algorithm with a Sigmoid kernel
- Detect faces in real-time using a sliding window approach with IoU-based NMS
- Recognize known persons and display their names on screen

## Project Structure
 
```
src/
├── Main.java                    # Entry point
├── core/
│   └── PreProcessingData.java   # Rescales dataset images to 128x128
├── gui/
│   ├── MainFrame.java           # Main application window
│   ├── CameraPanel.java         # Live camera feed with detection
│   ├── LearningSetPanel.java    # Webcam image collection interface
│   ├── TrainingConfigPanel.java # HOG extraction and SVM training
│   ├── CustomButton.java        # Styled AWT Button
│   ├── CustomLabel.java         # Styled AWT Label
│   ├── CustomTextArea.java      # Styled AWT TextArea
│   └── CustomTextField.java     # Styled AWT TextField with placeholder
├── hog/
│   └── HOG.java                 # HOG feature extractor
└── svm/
    ├── SMO.java                 # Sequential Minimal Optimization
    └── SVMClassifier.java       # SVM model with predict and serialize
```

## Algorithms
 
### HOG (Histogram of Oriented Gradients)
- Image converted to grayscale using ITU-R BT.601 weights
- Gradients computed using finite differences
- Image divided into 8x8 pixel cells
- 9-bin orientation histograms per cell (0-180 degrees)
- Cells grouped into 2x2 blocks with L2 normalization
- Final vector: 15x15x4x9 = 8100 features

### SMO (Sequential Minimal Optimization)
- Solves the SVM dual optimization problem
- Optimizes two Lagrange multipliers (alpha) per iteration
- KKT conditions used to select violating pairs
- Sigmoid kernel: K(a,b) = tanh(γ · dot(a,b) + coef0)
- One-vs-all strategy for multi-person recognition


### Sliding Window + NMS
- 128x128 window sliding over camera frame with step=32
- HOG extracted per window, classified by head detector SVM
- IoU-based grouping of overlapping detections
- Best group selected by density (most overlapping windows)
- Average coordinates used as final bounding box position


## Requirements
 
- Java JDK 17+
- OpenCV 4.6.0 (for webcam capture and drawing only)

## Usage
 
### 1. Prepare Face Detector Dataset
- Download a cropped face dataset (e.g. from Kaggle) into `data/raw/Humans/`
- Download a non-face dataset into `data/raw/archive/`
- Run `PreProcessingData.java` to rescale all images to 128x128:
```
bash
javac src/core/PreProcessingData.java
java -cp src/core PreProcessingData
```
This creates `data/raw/faces/` and `data/raw/non_faces/` with rescaled images.
 
### 2. Collect Training Images
- Open **Learning Set** tab
- Enter person name and click **New Set**
- Use **Take Picture** (manual) or switch to **Auto** mode with a delay
- Review and delete bad images with **< Delete >**
- Click **Save** when done — repeat for each person

### 3. Train Models
- Open **Training Config** tab
- Click **Extract HOG** — extracts HOG vectors for all collected persons
- Click **Train Face Detector** — trains SVM on faces vs non-faces
- Click **Train Face Recognition** — trains one SVM per person (one-vs-all)

### 4. Live Detection
- Open **Camera** tab
- Detected faces outlined with a bounding box
- Recognized persons labeled by name above the box

## Data Structure
```
data/
├── raw/
│   ├── faces/          # Rescaled positive samples (128x128)
│   ├── non_faces/      # Rescaled negative samples (128x128)
│   └── <person_name>/  # Per-person webcam images (128x128)
├── hog_training/       # Serialized HOG vectors (.hog files)
└── face_models/        # Trained SVM classifiers (.model files)
```

## Development
 
- Developed entirely in **Neovim** on Linux Mint
- Compiled and run with standard JDK (`javac` / `java`) — no IDE used
- OpenCV used **exclusively** for webcam capture and drawing rectangles
- All algorithms (HOG, SMO, sliding window, NMS) implemented from scratch in pure Java
