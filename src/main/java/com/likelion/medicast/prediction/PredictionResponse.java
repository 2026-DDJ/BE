package com.likelion.medicast.prediction;

import com.likelion.medicast.entity.Prediction;
import com.likelion.medicast.entity.PredictionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PredictionResponse(
        Long id,
        Long assessmentId,
        PredictionStatus status,
        BigDecimal predictedAnnualMedicalCost,
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
                CURRENCY,
                prediction.getModelVersion(),
                prediction.getErrorCode(),
                prediction.isRetryable(),
                prediction.getCreatedAt(),
                prediction.getCompletedAt()
        );
    }
}
