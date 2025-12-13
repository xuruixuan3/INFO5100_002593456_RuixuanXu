# Number Recognizer - Final Project

## Project Overview
A JavaFX-based handwritten digit recognition application that allows users to draw digits (0-9) on a canvas and uses a TensorFlow model to recognize them. Users can also save their drawn digits as training data to improve the model in future.

## Project Structure
```
NumberRecognizer/
├── src/
│   └── [Java source files]
│       ├── MainApplication.java
│       ├── DrawingController.java
│       ├── PredictionService.java
│       ├── TrainingDataSaver.java
│       ├── ServiceFactory.java
│       ├── BaseService.java
│       └── PredictionResult.java
├── mnist_model/           # Pre-trained TensorFlow model
├── labeled_data/          # Training data (auto-created)
├── output/                # Predictions output
├── predict.py             # Prediction script
├── save_training.py       # Training data saving script
├── image_utils.py         # Image processing utilities
├── README.md
└── pom.xml
```

## Key Features
- **Real-time Digit Recognition**: Draw digits and get instant predictions with confidence scores
- **Multi-digit Support**: Recognize multiple digits in a single image
- **Training Data Collection**: Save user-drawn digits to expand the training dataset
- **Single Digit Centering**: Automatic digit centering for single digit input
- **Comprehensive Error Handling**: Detailed error messages for various failure scenarios

## Architecture & Design Patterns

### Design Pattern: Singleton Factory
The project uses the **Singleton Factory Pattern**: `ServiceFactory` maintains a single instance and manages creation of `PredictionService` and `TrainingDataSaver`, which extend `BaseService`.

### Key Components
- **DrawingController**: UI controller handling canvas interactions and dialog flows
- **PredictionService**: Wraps `predict.py` for digit recognition
- **TrainingDataSaver**: Wraps `save_training.py` for saving labeled training data
- **PredictionResult**: Data model for prediction results
- **BaseService**: Base class managing Python subprocess execution

## System Requirements
- Java 24
- JavaFX 21.0.6
- Python 3.7+
- TensorFlow 2.x
- Pillow (PIL)
- NumPy

## Installation & Setup

### 1. Install Python Dependencies
```bash
pip install tensorflow pillow numpy
```

### 2. Build and Run (Maven)
```bash
mvn clean javafx:run
```

## Usage

### Predicting Digits
1. Draw digits on the canvas using mouse
2. Click "Predict" button
3. View prediction results with confidence scores
4. For multiple digits, results are displayed in order from left to right

### Saving Training Data
1. After prediction, click "Save" button
2. Verify the predicted digits (or correct them)
3. Confirm to save - digits are automatically extracted and saved to `labeled_data/{digit}/`
4. Each digit is saved as a PNG with unique MD5-based filename

## Python Scripts

### predict.py
Recognizes digits in an image.
- **Input**: Image path
- **Output**: Predicted digit(s) and confidence scores
- **Handles**: Single digit (with centering) and multi-digit recognition

### save_training.py
Saves user-drawn digits as labeled training data.
- **Input**: Image path, correct digit labels (user-confirmed)
- **Output**: Labeled PNG files in `labeled_data/{label}/`
- **Validation**: Matches number of detected digits with provided labels

### image_utils.py
Image preprocessing utilities.
- Binary image conversion
- Connected component analysis
- Digit region extraction and normalization

## Error Handling
The application handles various error scenarios:
- No digits detected in image
- File not found
- Invalid label input
- Digit count mismatch between detection and user input
- Python subprocess failures

All errors are displayed to the user via dialog boxes.

## Testing

Screenshots of test cases are included in the project.

## Author
      Yuwei Ni  &  Ruixuan Xu
NUID: 002507246 &  002593456