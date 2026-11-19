package com.likelion.medicast.prediction.model;

import java.math.BigDecimal;

public record PredictionResult(
        BigDecimal predictedAnnualMedicalCost,
        BigDecimal sameIncomeGroupMean,
        BigDecimal sameOutpatientGroupMean,
        BigDecimal overallMean,
        String modelVersion,
        String featureSchemaVersion,
        String preprocessingVersion
) {
}
