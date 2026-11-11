package com.likelion.medicast.prediction.model;

import java.math.BigDecimal;

public record PredictionResult(
        BigDecimal predictedAnnualMedicalCost,
        BigDecimal highCostProbability,
        String modelVersion,
        String featureSchemaVersion,
        String preprocessingVersion
) {
}
