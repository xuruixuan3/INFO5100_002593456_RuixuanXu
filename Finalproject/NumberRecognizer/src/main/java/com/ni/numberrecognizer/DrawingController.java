package com.ni.numberrecognizer;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

import com.ni.numberrecognizer.service.*;
import com.ni.numberrecognizer.model.PredictionResult;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.awt.image.BufferedImage;


public class DrawingController {

    @FXML
    private Canvas drawCanvas;

    private double lastX, lastY;

    // Service references (via interfaces)
    private IPredictionService predictionService;
    private ITrainingDataSaver trainingDataSaver;

    @FXML
    public void initialize() {
        GraphicsContext gc = drawCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(7);

        drawCanvas.setOnMousePressed(e -> {
            lastX = e.getX();
            lastY = e.getY();
        });

        drawCanvas.setOnMouseDragged(e -> {
            double x = e.getX();
            double y = e.getY();
            gc.strokeLine(lastX, lastY, x, y);
            lastX = x;
            lastY = y;
        });

        // Get services from the factory
        ServiceFactory factory = ServiceFactory.getInstance();
        predictionService = factory.getPredictionService();
        trainingDataSaver = factory.getTrainingDataSaver();

        System.out.println("[DrawingController] Initialization completed");
    }

    @FXML
    private void onClear() {
        GraphicsContext gc = drawCanvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight());
    }

    // Predict button click event handler
    @FXML
    private void onPredict() {
        try {
            // Capture and save image
            File imageFile = captureAndSaveImage();

            // Call prediction service
            PredictionResult result = predictionService.predict(imageFile);

            // Show result
            showResultDialog(result);

            // Show confidence and ask whether to save
            if (result.isSuccess()) {
                showConfidenceDialog(result, imageFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showErrorAlert("Error", e.getMessage());
        }
    }


    // Capture and save image
    private File captureAndSaveImage() throws Exception {
        WritableImage img = new WritableImage(
                (int) drawCanvas.getWidth(),
                (int) drawCanvas.getHeight()
        );
        drawCanvas.snapshot(null, img);

        File folder = new File("output");
        if (!folder.exists()) folder.mkdir();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File fileOriginal = new File(folder, timestamp + "_original.png");
        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", fileOriginal);
        System.out.println("Saved: " + fileOriginal.getAbsolutePath());

        return fileOriginal;
    }

    // Show prediction result dialog
    private void showResultDialog(PredictionResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prediction Result");
        alert.setHeaderText(null);

        Label label = new Label("Result: " + result.getPredicted());
        label.setStyle("-fx-font-size: 32px; -fx-text-fill: " +
                (result.isSuccess() ? "#2ecc71" : "#e74c3c") +
                "; -fx-font-weight: bold;");

        alert.getDialogPane().setContent(label);
        alert.getDialogPane().setMinHeight(200);
        alert.getDialogPane().setMinWidth(350);

        alert.showAndWait();
    }

    // Show confidence dialog
    private void showConfidenceDialog(PredictionResult result, File imageFile) {

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confidence Details");
        confirmAlert.setHeaderText("Is this result correct?");

        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(5);

        Label predictedLabel = new Label("Predicted: " + result.getPredicted());
        predictedLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        content.getChildren().add(predictedLabel);

        // Process confidence information
        String confidencesText = result.getConfidences();
        if (confidencesText != null && !confidencesText.isEmpty()) {
            String[] confidenceLines = confidencesText.split(",");
            String[] digits = result.getPredicted().split("");

            for (int i = 0; i < digits.length && i < confidenceLines.length; i++) {
                String digit = digits[i];
                String confStr = confidenceLines[i].trim();

                try {
                    double confidence = Double.parseDouble(confStr);

                    Label digitLabel = new Label(digit + " (" + confStr + "%)");
                    digitLabel.setStyle("-fx-font-size: 14px; -fx-font-family: monospace;");

                    // Color based on confidence
                    if (confidence >= 90) {
                        digitLabel.setTextFill(javafx.scene.paint.Color.web("#2ecc71")); // green
                    } else if (confidence >= 70) {
                        digitLabel.setTextFill(javafx.scene.paint.Color.web("#f39c12")); // yellow
                    } else {
                        digitLabel.setTextFill(javafx.scene.paint.Color.web("#e74c3c")); // red
                    }

                    content.getChildren().add(digitLabel);
                } catch (NumberFormatException ignored) {}
            }
        }

        confirmAlert.getDialogPane().setContent(content);
        confirmAlert.getDialogPane().setMinHeight(300);
        confirmAlert.getDialogPane().setMinWidth(350);

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No");
        confirmAlert.getButtonTypes().setAll(yes, no);

        Optional<ButtonType> userChoice = confirmAlert.showAndWait();

        if (userChoice.isPresent() && userChoice.get() == no) {
            showCorrectionDialog(imageFile, result.getDigitCount());
        } else if (userChoice.isPresent() && userChoice.get() == yes) {
            showSaveConfirmDialog(imageFile, result.getPredicted());
        }
    }


    // Show correction dialog
    private void showCorrectionDialog(File imageFile, int expectedDigitCount) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Correct Answer");
        dialog.setHeaderText("Please enter " + expectedDigitCount + " digit(s):");
        dialog.setContentText("Correct digit(s):");

        Optional<String> correct = dialog.showAndWait();

        if (correct.isPresent()) {
            String userInput = correct.get().trim();

            // Only allow digits 0-9
            String regex = "^[0-9]+$";

            if (!userInput.matches(regex)) {
                showErrorAlert(
                        "Invalid Input",
                        "Please enter only digits (0-9).\n" +
                                "You entered: " + userInput + "\n" +
                                "Examples: '5', '123', '7890'"
                );
                showCorrectionDialog(imageFile, expectedDigitCount);
                return;
            }

            // Digit count check
            if (userInput.length() != expectedDigitCount) {
                showErrorAlert(
                        "Wrong Number of Digits",
                        "Expected " + expectedDigitCount + " digit(s), but you entered " + userInput.length() + ".\n" +
                                "You entered: " + userInput + "\n" +
                                "Please try again."
                );
                showCorrectionDialog(imageFile, expectedDigitCount);
                return;
            }

            System.out.println("User correction: " + userInput + " (expected " + expectedDigitCount + " digits)");

            try {
                boolean saved = trainingDataSaver.save(imageFile, userInput);
                if (saved) {
                    showSuccessAlert("Data Saved", "Training data saved successfully");
                } else {
                    showErrorAlert("Save Failed", "Failed to save training data");
                }
            } catch (Exception e) {
                showErrorAlert("Error", e.getMessage());
            }
        }
    }

    // Show save confirmation dialog
    private void showSaveConfirmDialog(File imageFile, String predicted) {
        Alert saveAlert = new Alert(Alert.AlertType.CONFIRMATION);
        saveAlert.setTitle("Save Training Data");
        saveAlert.setHeaderText("Save this as re-training data?");

        Label label = new Label(
                "Save '" + predicted + "' as re-training data?\n"
        );
        label.setStyle("-fx-font-size: 14px;");
        label.setWrapText(true);

        saveAlert.getDialogPane().setContent(label);
        saveAlert.getDialogPane().setMinHeight(200);
        saveAlert.getDialogPane().setMinWidth(350);

        ButtonType save = new ButtonType("Save");
        ButtonType skip = new ButtonType("Skip");
        saveAlert.getButtonTypes().setAll(save, skip);

        Optional<ButtonType> result = saveAlert.showAndWait();

        if (result.isPresent() && result.get() == save) {
            // Save training data
            try {
                boolean saved = trainingDataSaver.save(imageFile, predicted);
                if (saved) {
                    showSuccessAlert("Data Saved", "re-Training data saved successfully");
                } else {
                    showErrorAlert("Save Failed", "Failed to save training data");
                }
            } catch (Exception e) {
                showErrorAlert("Error", e.getMessage());
            }
        }
    }

    // Show success alert
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Show error alert
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}