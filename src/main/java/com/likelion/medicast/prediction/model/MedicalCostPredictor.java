package com.likelion.medicast.prediction.model;

import com.likelion.medicast.assessment.entity.HealthAssessment;

public interface MedicalCostPredictor {

    PredictionResult predict(HealthAssessment assessment);
}
