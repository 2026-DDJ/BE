package com.likelion.medicast.assessment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Occupation {
    MANAGER("14"),
    PROFESSIONAL("24"),
    OFFICE_WORKER("31"),
    SERVICE_WORKER("42"),
    SALES_WORKER("52"),
    AGRICULTURE_FISHERY("61"),
    CRAFT_WORKER("78"),
    MACHINE_OPERATOR_ASSEMBLER("87"),
    ELEMENTARY_WORKER("94"),
    NOT_ECONOMICALLY_ACTIVE("none");

    private final String frontendValue;

    Occupation(String frontendValue) {
        this.frontendValue = frontendValue;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Occupation fromFrontendValue(Object value) {
        String raw = String.valueOf(value);
        for (Occupation candidate : values()) {
            if (candidate.frontendValue.equals(raw) || candidate.name().equals(raw)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 직업군입니다.");
    }

    @JsonValue
    public String frontendValue() {
        return frontendValue;
    }
}
