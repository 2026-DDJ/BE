package com.likelion.medicast.prediction.model;

import com.likelion.medicast.entity.HealthAssessment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Profile({"local", "test"})
public class DeterministicFakeMedicalCostPredictor implements MedicalCostPredictor {

    private static final BigDecimal BASE_COST = new BigDecimal("300000.00");

    @Override
    public PredictionResult predict(HealthAssessment assessment) {
        BigDecimal cost = BASE_COST
                .add(BigDecimal.valueOf(assessment.getAge()).multiply(new BigDecimal("12000")))
                .add(assessment.getBmi().multiply(new BigDecimal("4000")))
                .add(assessment.isHasChronicDisease() ? new BigDecimal("600000") : BigDecimal.ZERO)
                .add(BigDecimal.valueOf(assessment.getSelfRatedHealth().ordinal())
                        .multiply(new BigDecimal("180000")));

        return new PredictionResult(
                cost.setScale(2, RoundingMode.HALF_UP),
                "deterministic-fake-v1",
                "legacy-assessment-v1",
                "deterministic-fake-preprocessing-v1"
        );
    }
}
