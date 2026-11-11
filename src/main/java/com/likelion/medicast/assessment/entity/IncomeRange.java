package com.likelion.medicast.assessment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IncomeRange {
    UNDER_2400("under-2400"),
    FROM_2400_TO_4500("2400-4500"),
    FROM_4500_TO_7500("4500-7500"),
    OVER_7500("over-7500");

    private final String frontendValue;

    IncomeRange(String frontendValue) {
        this.frontendValue = frontendValue;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static IncomeRange fromFrontendValue(Object value) {
        String raw = String.valueOf(value);
        for (IncomeRange candidate : values()) {
            if (candidate.frontendValue.equals(raw) || candidate.name().equals(raw)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 소득 구간입니다.");
    }

    @JsonValue
    public String frontendValue() {
        return frontendValue;
    }
}
