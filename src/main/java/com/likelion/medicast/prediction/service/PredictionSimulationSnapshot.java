package com.likelion.medicast.prediction.service;

import com.likelion.medicast.prediction.model.ModelFeatures;

import java.math.BigDecimal;

public record PredictionSimulationSnapshot(
        Long predictionId,
        BigDecimal heightCm,
        BigDecimal bmi,
        boolean exercises,
        BigDecimal predictedMedicalCost,
        ModelFeatures modelFeatures
) {
}
