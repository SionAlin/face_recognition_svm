# Face Recognition SVM
 
A Java-based face detection and recognition system using Support Vector Machines (SVM) and Histogram of Oriented Gradients (HOG).
 
## Overview
 
This project implements a complete face detection and recognition pipeline from scratch, without using any pre-trained models or specialized ML libraries. The system can:
- Collect training images via webcam
- Extract HOG feature vectors from images
- Train SVM classifiers using the SMO algorithm with a Sigmoid kernel
- Detect faces in real-time using a sliding window approach
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
- Image converted to grayscale
- Gradients computed using finite differences
- Image divided into 8x8 pixel cells
- 9-bin orientation histograms per cell
- Cells grouped into 2x2 blocks with L2 normalization
- Final vector: 15x15x4x9 = 8100 features
### SMO (Sequential Minimal Optimization)
- Solves the SVM dual optimization problem
- Optimizes two Lagrange multipliers (alpha) per iteration
- KKT conditions used to select violating pairs
- Sigmoid kernel: K(a,b) = tanh(γ · dot(a,b) + coef0)
- One-vs-all strategy for multi-person recognition
### Sliding Window
- 128x128 window sliding over camera frame
- Step size of 16 pixels
- HOG extracted per window
- Head detector SVM classifies each window
- Recognized persons labeled in green
## Requirements
 
- Java JDK 17+
- OpenCV 4.6.0 (for webcam capture only)
## Usage
 
### 1. Collect Training Images
- Open **Learning Set** tab
- Enter person name and click **New Set**
- Use **Take Picture** (manual) or **Auto** mode to collect 500 images
- Review and delete bad images with **< Delete >**
- Click **Save** when done
### 2. Prepare Dataset for Face Detector
- Download a face dataset (e.g. from Kaggle) into `data/raw/faces/`
- Download a non-face dataset into `data/raw/non_faces/`
- Run `PreProcessingData.java` to rescale all images to 128x128
### 3. Train Models
- Open **Training Config** tab
- Click **Extract HOG** — extracts HOG vectors for all persons
- Click **Train Face Detector** — trains SVM on faces vs non-faces
- Click **Train Face Recognition** — trains one SVM per person
### 4. Run Live Detection
- Open **Camera** tab
- Detected faces are outlined in green
- Recognized persons are labeled by name
## Data Structure
 
```
data/
├── raw/
│   ├── faces/          # Positive samples for face detector
│   ├── non_faces/      # Negative samples for face detector
│   └── <person_name>/  # Per-person webcam images
├── hog_training/       # Serialized HOG vectors (.hog files)
└── face_models/        # Trained SVM classifiers (.model files)
```
 
## Notes
 
- All algorithms implemented from scratch in pure Java
- OpenCV used exclusively for webcam capture and drawing
- No pre-trained models or ML libraries used
- Models serialized with Java ObjectOutputStream
 
