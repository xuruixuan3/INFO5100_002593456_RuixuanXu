package com.ni.numberrecognizer.service;

/**
 * Base service class
 * Extended by PredictionService and TrainingDataSaver
 */
public abstract class BaseService {

    protected String pythonCommand;
    protected String pythonScript;

    public BaseService() {
        initializePython();
    }


    protected void initializePython() {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        this.pythonCommand = isWindows ? "python" : "python3.10";
    }

    // Getters
    public String getPythonCommand() {
        return pythonCommand;
    }

    public String getPythonScript() {
        return pythonScript;
    }
}