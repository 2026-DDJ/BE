package com.likelion.medicast.prediction.model;

import java.math.BigDecimal;

public record PredictionResult(
        BigDecimal predictedAnnualMedicalCost,
        BigDecimal highCostProbability,
        String modelVersion,
        String featureSchemaVersion,
        String preprocessingVersion,
        BigDecimal sameIncomeGroupAverageAnnualMedicalCost,
        BigDecimal sameOutpatientVisitsGroupAverageAnnualMedicalCost,
        BigDecimal overallAverageAnnualMedicalCost
) {
    public PredictionResult(BigDecimal predictedAnnualMedicalCost, BigDecimal highCostProbability,
            String modelVersion, String featureSchemaVersion, String preprocessingVersion) {
        this(predictedAnnualMedicalCost, highCostProbability, modelVersion,
                featureSchemaVersion, preprocessingVersion, null, null, null);
    }
}
