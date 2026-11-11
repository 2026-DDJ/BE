package com.likelion.medicast.prediction.dto;

import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PredictionResponse(
        Long id,
        Long assessmentId,
        PredictionStatus status,
        BigDecimal predictedAnnualMedicalCost,
        BigDecimal highCostProbability,
        String currency,
        String modelVersion,
        String errorCode,
        boolean retryable,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {

    private static final String CURRENCY = "KRW";

    public static PredictionResponse from(Prediction prediction) {
        return new PredictionResponse(
                prediction.getId(),
                prediction.getAssessment().getId(),
                prediction.getStatus(),
                prediction.getPredictedMedicalCost(),
                prediction.getHighCostProbability(),
                CURRENCY,
                prediction.getModelVersion(),
                prediction.getErrorCode(),
                prediction.isRetryable(),
                prediction.getCreatedAt(),
                prediction.getCompletedAt()
        );
    }
}
