package com.likelion.medicast.assessment.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendEnumValueTest {

    @Test
    void acceptsEveryFrontendIncomeValue() {
        String[] values = {"under-2400", "2400-4500", "4500-7500", "over-7500"};
        assertThat(values).allSatisfy(value ->
                assertThat(IncomeRange.fromFrontendValue(value).frontendValue()).isEqualTo(value));
    }

    @Test
    void acceptsEveryFrontendInsuranceValue() {
        String[] values = {"1", "2", "3", "4", "5", "6", "7", "none"};
        assertThat(values).allSatisfy(value ->
                assertThat(HealthInsuranceType.fromFrontendValue(value).frontendValue()).isEqualTo(value));
    }

    @Test
    void acceptsEveryFrontendRegionValue() {
        String[] values = {
                "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시",
                "대전광역시", "울산광역시", "세종특별자치시", "경기도", "강원특별자치도",
                "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도",
                "경상남도", "제주특별자치도"
        };
        assertThat(values).allSatisfy(value ->
                assertThat(Region.fromFrontendValue(value).frontendValue()).isEqualTo(value));
    }

    @Test
    void acceptsEveryFrontendOccupationValue() {
        String[] values = {"14", "24", "31", "42", "52", "61", "78", "87", "94", "none"};
        assertThat(values).allSatisfy(value ->
                assertThat(Occupation.fromFrontendValue(value).frontendValue()).isEqualTo(value));
    }
}
