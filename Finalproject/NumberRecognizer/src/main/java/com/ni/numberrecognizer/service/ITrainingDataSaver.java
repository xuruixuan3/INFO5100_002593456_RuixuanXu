package com.ni.numberrecognizer.service;

import java.io.File;


public interface ITrainingDataSaver {
    /**
     * @param imageFile original image file
     * @param correctLabel user-labeled correct digit
     */
    boolean save(File imageFile, String correctLabel);
}