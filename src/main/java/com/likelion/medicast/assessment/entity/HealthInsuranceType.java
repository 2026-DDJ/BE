package com.likelion.medicast.assessment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum HealthInsuranceType {
    EMPLOYEE_INSURED("1"),
    EMPLOYEE_DEPENDENT("2"),
    LOCAL_HOUSEHOLDER("3"),
    LOCAL_MEMBER("4"),
    MEDICAL_AID_HOUSEHOLDER("5"),
    MEDICAL_AID_MEMBER("6"),
    PATRIOT_VETERAN("7"),
    UNINSURED("none");

    private final String frontendValue;

    HealthInsuranceType(String frontendValue) {
        this.frontendValue = frontendValue;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static HealthInsuranceType fromFrontendValue(Object value) {
        String raw = String.valueOf(value);
        for (HealthInsuranceType candidate : values()) {
            if (candidate.frontendValue.equals(raw) || candidate.name().equals(raw)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 건강보험 유형입니다.");
    }

    @JsonValue
    public String frontendValue() {
        return frontendValue;
    }
}
