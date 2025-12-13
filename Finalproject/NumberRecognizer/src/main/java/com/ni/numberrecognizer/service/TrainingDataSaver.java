package com.ni.numberrecognizer.service;

import java.io.*;

/**
 * Training data saver implementation class
 * Extends BaseService and implements ITrainingDataSaver interface
 * Responsible for calling the Python script to save training data
 */
public class TrainingDataSaver extends BaseService implements ITrainingDataSaver {

    // Constructor
    public TrainingDataSaver() {
        super();
        this.pythonScript = "save_training.py";
        System.out.println("[TrainingDataSaver] Initialization completed, pythonScript = " + pythonScript);
    }

    // Save training data
    @Override
    public boolean save(File imageFile, String correctLabel) {
        try {
            // Validate parameters
            if (imageFile == null || !imageFile.exists()) {
                System.out.println("[TrainingDataSaver] Error: Image file does not exist");
                return false;
            }
            if (correctLabel == null || correctLabel.trim().isEmpty()) {
                System.out.println("[TrainingDataSaver] Error: Label is empty");
                return false;
            }

            callPythonSave(imageFile, correctLabel);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Call Python save script
    private void callPythonSave(File imageFile, String correctLabel)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                pythonScript,
                imageFile.getAbsolutePath(),
                correctLabel
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
        );
        String line;

        while ((line = reader.readLine()) != null) {
            System.out.println("[SaveScript] " + line);
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Save script execution failed");
        }
    }
}
