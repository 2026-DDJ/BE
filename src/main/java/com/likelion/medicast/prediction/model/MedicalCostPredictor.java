package com.likelion.medicast.prediction.model;

public interface MedicalCostPredictor {

    PredictionResult predict(ModelFeatures features);
}
