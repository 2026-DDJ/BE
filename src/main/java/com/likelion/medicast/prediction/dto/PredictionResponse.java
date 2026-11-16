package com.likelion.medicast.prediction.dto;

import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PredictionResponse(
        @Schema(description = "예측 ID", example = "1", readOnly = true)
        Long id,

        @Schema(description = "예측에 사용된 평가 ID", example = "1", readOnly = true)
        Long assessmentId,

        @Schema(
                description = "예측 상태",
                example = "COMPLETED",
                allowableValues = {"PENDING", "COMPLETED", "FAILED"},
                readOnly = true
        )
        PredictionStatus status,

        @Schema(
                description = "예상 연간 의료비. COMPLETED가 아니면 null입니다.",
                example = "1250000.00",
                minimum = "0",
                nullable = true,
                readOnly = true
        )
        BigDecimal predictedAnnualMedicalCost,

        @Schema(
                description = "고비용군 확률(0~1). COMPLETED가 아니면 null입니다.",
                example = "0.15",
                minimum = "0",
                maximum = "1",
                nullable = true,
                readOnly = true
        )
        BigDecimal highCostProbability,

        @Schema(description = "예측 금액 통화", example = "KRW", allowableValues = "KRW", readOnly = true)
        String currency,

        @Schema(description = "모델 버전. FAILED이면 null일 수 있습니다.", example = "xgboost-v1", nullable = true, readOnly = true)
        String modelVersion,

        @Schema(description = "안정적인 실패 코드. 성공 시 null입니다.", example = "MODEL_UNAVAILABLE", nullable = true, readOnly = true)
        String errorCode,

        @Schema(description = "동일 요청을 재시도해도 되는지 여부", example = "false", readOnly = true)
        boolean retryable,

        @Schema(description = "예측 생성 시각(UTC)", example = "2026-11-16T04:00:00", format = "date-time", readOnly = true)
        LocalDateTime createdAt,

        @Schema(description = "예측 처리 완료 시각(UTC)", example = "2026-11-16T04:00:01", format = "date-time", nullable = true, readOnly = true)
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
