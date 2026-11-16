package com.likelion.medicast.assessment.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BodyMassIndexCalculatorTest {

    private final BodyMassIndexCalculator calculator = new BodyMassIndexCalculator();

    @Test
    void calculatesBmiFromCentimetersAndKilogramsAtFourDecimalPlaces() {
        assertThat(calculator.calculate(new BigDecimal("170"), new BigDecimal("65")))
                .isEqualByComparingTo("22.4913");
    }

    @Test
    void calculatesWeightForTargetBmiAtFourDecimalPlaces() {
        assertThat(calculator.weightForBmi(new BigDecimal("170"), new BigDecimal("20")))
                .isEqualByComparingTo("57.8000");
    }
}
