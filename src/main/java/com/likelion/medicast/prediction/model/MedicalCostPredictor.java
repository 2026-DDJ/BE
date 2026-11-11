package com.likelion.medicast.prediction.model;

import com.likelion.medicast.entity.HealthAssessment;

public interface MedicalCostPredictor {

    PredictionResult predict(HealthAssessment assessment);
}
