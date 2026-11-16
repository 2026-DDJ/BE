package com.likelion.medicast.prediction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record SimulationResponse(
        @Schema(description = "비교 기준이 된 기존 예측 ID", example = "1", readOnly = true)
        Long predictionId,

        @Schema(description = "기존 입력 조건과 저장된 예측 결과", readOnly = true)
        SimulationConditionResponse before,

        @Schema(description = "변경 조건과 새 AI 예측 결과", readOnly = true)
        SimulationConditionResponse after,

        @Schema(description = "변경 후 - 변경 전 연간 의료비(원)", example = "-120000.00", readOnly = true)
        BigDecimal annualMedicalCostDifference,

        @Schema(description = "통화", example = "KRW", allowableValues = "KRW", readOnly = true)
        String currency,

        @Schema(description = "변경 후 예측에 사용된 모델 버전", example = "external-model-v1", readOnly = true)
        String modelVersion
) {
}
