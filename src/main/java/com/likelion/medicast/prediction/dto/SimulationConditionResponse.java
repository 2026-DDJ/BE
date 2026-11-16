package com.likelion.medicast.prediction.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record SimulationConditionResponse(
        @Schema(description = "조건의 BMI", example = "20.0", readOnly = true)
        BigDecimal bmi,

        @Schema(description = "규칙적 운동 여부", example = "true", readOnly = true)
        boolean exercises,

        @Schema(description = "예상 연간 의료비(원)", example = "1250000.00", minimum = "0", readOnly = true)
        BigDecimal predictedAnnualMedicalCost,

        @Schema(description = "고비용군 확률(0~1)", example = "0.15", minimum = "0", maximum = "1", readOnly = true)
        BigDecimal highCostProbability
) {
}
