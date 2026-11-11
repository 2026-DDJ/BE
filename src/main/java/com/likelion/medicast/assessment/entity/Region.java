package com.likelion.medicast.assessment.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Region {
    SEOUL("서울특별시"),
    BUSAN("부산광역시"),
    DAEGU("대구광역시"),
    INCHEON("인천광역시"),
    GWANGJU("광주광역시"),
    DAEJEON("대전광역시"),
    ULSAN("울산광역시"),
    SEJONG("세종특별자치시"),
    GYEONGGI("경기도"),
    GANGWON("강원특별자치도"),
    CHUNGBUK("충청북도"),
    CHUNGNAM("충청남도"),
    JEONBUK("전북특별자치도"),
    JEONNAM("전라남도"),
    GYEONGBUK("경상북도"),
    GYEONGNAM("경상남도"),
    JEJU("제주특별자치도");

    private final String frontendValue;

    Region(String frontendValue) {
        this.frontendValue = frontendValue;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Region fromFrontendValue(Object value) {
        String raw = String.valueOf(value);
        for (Region candidate : values()) {
            if (candidate.frontendValue.equals(raw) || candidate.name().equals(raw)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 거주 지역입니다.");
    }

    @JsonValue
    public String frontendValue() {
        return frontendValue;
    }
}
