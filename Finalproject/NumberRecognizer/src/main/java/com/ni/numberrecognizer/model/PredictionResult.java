package com.ni.numberrecognizer.model;

// Encapsulates prediction result data

public class PredictionResult {
    private String predicted;           // Predicted result
    private String confidences;         // Confidence of each digit
    private boolean isSuccess;          // Whether prediction is successful
    private String errorMessage;        // Error message
    private int digitCount;             // Number of detected digits

    /**
     * @param predicted predicted digit result
     * @param confidences confidence information
     */
    public PredictionResult(String predicted, String confidences) {
        this.predicted = predicted;
        this.confidences = confidences;
        this.isSuccess = true;
        this.errorMessage = null;
        this.digitCount = predicted != null ? predicted.length() : 0;
    }

    public PredictionResult(String errorMessage) {
        this.predicted = null;
        this.confidences = null;
        this.isSuccess = false;
        this.errorMessage = errorMessage;
    }

    public String getPredicted() {
        return predicted;
    }

    public String getConfidences() {
        return confidences;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getDigitCount() {
        return digitCount;
    }

    @Override
    public String toString() {
        if (isSuccess) {
            return "PredictionResult{" +
                    "predicted='" + predicted + '\'' +
                    ", confidences='" + confidences + '\'' +
                    '}';
        } else {
            return "PredictionResult{error='" + errorMessage + "'}";
        }
    }
}