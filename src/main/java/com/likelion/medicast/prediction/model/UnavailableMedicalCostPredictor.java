package com.likelion.medicast.prediction.model;

import com.likelion.medicast.entity.HealthAssessment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!local & !test")
public class UnavailableMedicalCostPredictor implements MedicalCostPredictor {

    @Override
    public PredictionResult predict(HealthAssessment assessment) {
        throw new PredictionProviderException("MODEL_UNAVAILABLE", true);
    }
}
