package com.likelion.medicast.assessment.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class BodyMassIndexCalculator {

    private static final BigDecimal CENTIMETERS_PER_METER_SQUARED = new BigDecimal("10000");
    private static final int BMI_SCALE = 4;
    private static final int WEIGHT_SCALE = 4;

    public BigDecimal calculate(BigDecimal heightCm, BigDecimal weightKg) {
        return weightKg.multiply(CENTIMETERS_PER_METER_SQUARED)
                .divide(heightCm.multiply(heightCm), BMI_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal weightForBmi(BigDecimal heightCm, BigDecimal bmi) {
        return bmi.multiply(heightCm.multiply(heightCm))
                .divide(CENTIMETERS_PER_METER_SQUARED, WEIGHT_SCALE, RoundingMode.HALF_UP);
    }
}
