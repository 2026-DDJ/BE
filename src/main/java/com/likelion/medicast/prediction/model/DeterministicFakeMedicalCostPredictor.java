package com.likelion.medicast.prediction.model;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Profile({"local", "test"})
public class DeterministicFakeMedicalCostPredictor implements MedicalCostPredictor {

    private static final BigDecimal BASE_COST = new BigDecimal("300000.00");

    @Override
    public PredictionResult predict(ModelFeatures features) {
        BigDecimal cost = BASE_COST
                .add(features.weight().multiply(new BigDecimal("4000")))
                .add(BigDecimal.valueOf(features.ougun())
                        .multiply(new BigDecimal("25000")))
                .add(BigDecimal.valueOf(features.hospitalizedYear())
                        .multiply(new BigDecimal("600000")))
                .add(BigDecimal.valueOf(features.selfRatedHealth() - 1L)
                        .multiply(new BigDecimal("180000")));

        return new PredictionResult(
                cost.setScale(2, RoundingMode.HALF_UP),
                new BigDecimal("0.1500"),
                "deterministic-fake-v1",
                ModelFeatureMapper.FEATURE_SCHEMA_VERSION,
                "notion-15-fake-preprocessing-v1"
        );
    }
}
