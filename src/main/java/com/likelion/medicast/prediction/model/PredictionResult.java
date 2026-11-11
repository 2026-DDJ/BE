package com.likelion.medicast.prediction.model;

import java.math.BigDecimal;

public record PredictionResult(
        BigDecimal predictedAnnualMedicalCost,
        String modelVersion,
        String featureSchemaVersion,
        String preprocessingVersion
) {
}
