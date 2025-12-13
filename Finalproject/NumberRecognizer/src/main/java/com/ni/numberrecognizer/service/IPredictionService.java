package com.ni.numberrecognizer.service;

import com.ni.numberrecognizer.model.PredictionResult;

import java.io.File;

public interface IPredictionService {
    /**
     * @param imageFile input image file
     * @return prediction result
     */
    PredictionResult predict(File imageFile);
}