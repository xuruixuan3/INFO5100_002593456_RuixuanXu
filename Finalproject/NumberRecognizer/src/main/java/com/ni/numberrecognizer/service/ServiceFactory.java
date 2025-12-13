package com.ni.numberrecognizer.service;

// Singleton + Factory pattern

public class ServiceFactory {

    private static ServiceFactory instance;

    private IPredictionService predictionService;
    private ITrainingDataSaver trainingDataSaver;

    // Private constructor
    private ServiceFactory() {
        System.out.println("[ServiceFactory] Initialization started");

        // Create prediction service
        this.predictionService = new PredictionService();
        System.out.println("[ServiceFactory] PredictionService Initialization completed");

        // Create training data saver service
        this.trainingDataSaver = new TrainingDataSaver();
        System.out.println("[ServiceFactory] TrainingDataSaver Initialization completed");

        System.out.println("[ServiceFactory] All service initialization completed");
    }

    // Get singleton instance
    public static ServiceFactory getInstance() {
        // First check
        if (instance == null) {
            synchronized (ServiceFactory.class) {
                // Second check
                if (instance == null) {
                    // Create instance only here
                    instance = new ServiceFactory();
                }
            }
        }
        return instance;
    }

    // Get prediction service
    public IPredictionService getPredictionService() {
        return predictionService;
    }

    // Get training data saver service
    public ITrainingDataSaver getTrainingDataSaver() {
        return trainingDataSaver;
    }
}