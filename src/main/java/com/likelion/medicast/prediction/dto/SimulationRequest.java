package com.likelion.medicast.prediction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SimulationRequest(
        @Schema(description = "변경 후 BMI", example = "20.0", minimum = "15", maximum = "40")
        @NotNull(message = "변경 후 BMI는 필수입니다.")
        @DecimalMin(value = "15", message = "BMI는 15 이상이어야 합니다.")
        @DecimalMax(value = "40", message = "BMI는 40 이하여야 합니다.")
        BigDecimal bmi,

        @Schema(description = "변경 후 규칙적 운동 여부", example = "true")
        @NotNull(message = "변경 후 운동 여부는 필수입니다.")
        Boolean exercises
) {
}
