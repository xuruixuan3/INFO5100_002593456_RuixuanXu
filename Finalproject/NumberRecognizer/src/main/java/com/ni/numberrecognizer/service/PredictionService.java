package com.ni.numberrecognizer.service;

import com.ni.numberrecognizer.model.PredictionResult;

import java.io.*;

/**
 * Extends BaseService and implements IPredictionService interface
 * Responsible for calling the Python script to perform digit prediction
 */
public class PredictionService extends BaseService implements IPredictionService {

    // Constructor
    public PredictionService() {
        super();  // Initialize the Python environment
        this.pythonScript = "predict.py";
        System.out.println("[PredictionService] Initialization completed，pythonScript = " + pythonScript);
    }

    @Override
    public PredictionResult predict(File imageFile) {
        try {
            String result = callPythonPredict(imageFile);
            return parseResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            return new PredictionResult("Prediction Error: " + e.getMessage());
        }
    }

    // Call the Python prediction script
    private String callPythonPredict(File imageFile) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                pythonScript,
                imageFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        String predicted = null;
        String confidences = null;

        while ((line = reader.readLine()) != null) {
            System.out.println("[Python] " + line);
            if (line.startsWith("CONFIDENCES:")) {
                confidences = line.replace("CONFIDENCES:", "");
            } else if (!line.startsWith("[")) {
                predicted = line;
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0 || predicted == null) {
            throw new RuntimeException("Python script execution failed");
        }

        return predicted + "|" + (confidences != null ? confidences : "");
    }

    // Parse the output of the Python script
    private PredictionResult parseResult(String result) {
        String[] parts = result.split("\\|");
        String predicted = parts[0];
        String confidences = parts.length > 1 ? parts[1] : "";

        boolean isSuccess = predicted.matches("\\d+");

        if (isSuccess) {
            return new PredictionResult(predicted, confidences);
        }
        return new PredictionResult("Invalid result format");
    }
}