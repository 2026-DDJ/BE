package com.likelion.medicast.assessment.dto;

import com.likelion.medicast.assessment.entity.ActivityLimitation;
import com.likelion.medicast.assessment.entity.AnxietyDepression;
import com.likelion.medicast.assessment.entity.HealthInsuranceType;
import com.likelion.medicast.assessment.entity.IncomeRange;
import com.likelion.medicast.assessment.entity.Occupation;
import com.likelion.medicast.assessment.entity.Region;
import com.likelion.medicast.assessment.entity.SelfRatedHealth;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record HealthAssessmentInput(
        @Schema(description = "가구원 수", example = "3", minimum = "1", maximum = "10")
        @NotNull(message = "가구원 수는 필수입니다.")
        @Min(value = 1, message = "가구원 수는 1명 이상이어야 합니다.")
        @Max(value = 10, message = "가구원 수는 10명 이하여야 합니다.")
        Integer householdSize,

        @Schema(
                description = "소득 구간. 수치 단위는 모델 담당자 확정 전입니다.",
                example = "2400-4500",
                allowableValues = {"under-2400", "2400-4500", "4500-7500", "over-7500"}
        )
        @NotNull(message = "소득 구간은 필수입니다.")
        IncomeRange incomeRange,

        @Schema(
                description = "건강보험 유형: 1 직장가입자, 2 직장피부양자, 3 지역세대주, 4 지역세대원, "
                        + "5 의료급여 세대주, 6 의료급여 세대원, 7 국가유공자, none 미가입",
                example = "1",
                allowableValues = {"1", "2", "3", "4", "5", "6", "7", "none"}
        )
        @NotNull(message = "건강보험 유형은 필수입니다.")
        HealthInsuranceType healthInsuranceType,

        @Schema(
                description = "거주 시·도",
                example = "서울특별시",
                allowableValues = {
                        "서울특별시", "부산광역시", "대구광역시", "인천광역시", "광주광역시", "대전광역시", "울산광역시",
                        "세종특별자치시", "경기도", "강원특별자치도", "충청북도", "충청남도", "전북특별자치도", "전라남도", "경상북도",
                        "경상남도", "제주특별자치도"
                }
        )
        @NotNull(message = "거주 지역은 필수입니다.")
        Region region,

        @Schema(
                description = "주관적 건강상태",
                example = "FAIR",
                allowableValues = {"VERY_GOOD", "GOOD", "FAIR", "POOR", "VERY_POOR"}
        )
        @NotNull(message = "주관적 건강상태는 필수입니다.")
        SelfRatedHealth selfRatedHealth,

        @Schema(
                description = "키(cm). 제품 상한과 소수점 정책은 아직 미확정입니다.",
                example = "170.0",
                minimum = "0",
                exclusiveMinimum = true
        )
        @NotNull(message = "키는 필수입니다.")
        @DecimalMin(value = "0", inclusive = false, message = "키는 0보다 커야 합니다.")
        BigDecimal heightCm,

        @Schema(
                description = "몸무게(kg). 제품 상한과 소수점 정책은 아직 미확정입니다.",
                example = "65.0",
                minimum = "0",
                exclusiveMinimum = true
        )
        @NotNull(message = "몸무게는 필수입니다.")
        @DecimalMin(value = "0", inclusive = false, message = "몸무게는 0보다 커야 합니다.")
        BigDecimal weightKg,

        @Schema(description = "운동 여부", example = "true")
        @NotNull(message = "운동 여부는 필수입니다.")
        Boolean exercises,

        @Schema(
                description = "최근 1년 외래 이용 횟수. 기술적 상한은 아직 미확정입니다.",
                example = "4",
                minimum = "0"
        )
        @NotNull(message = "최근 1년 외래 이용 횟수는 필수입니다.")
        @PositiveOrZero(message = "최근 1년 외래 이용 횟수는 0 이상이어야 합니다.")
        Integer outpatientVisitsLastYear,

        @Schema(
                description = "최근 1년 치과 이용 횟수. 기술적 상한은 아직 미확정입니다.",
                example = "1",
                minimum = "0"
        )
        @NotNull(message = "최근 1년 치과 이용 횟수는 필수입니다.")
        @PositiveOrZero(message = "최근 1년 치과 이용 횟수는 0 이상이어야 합니다.")
        Integer dentalVisitsLastYear,

        @Schema(description = "실손보험 가입 여부", example = "true")
        @NotNull(message = "실손보험 가입 여부는 필수입니다.")
        Boolean hasPrivateHealthInsurance,

        @Schema(description = "최근 1년 입원 여부", example = "false")
        @NotNull(message = "최근 1년 입원 여부는 필수입니다.")
        Boolean hospitalizedLastYear,

        @Schema(
                description = "활동 제한 정도",
                example = "NONE",
                allowableValues = {"NONE", "SOME", "SEVERE"}
        )
        @NotNull(message = "활동 제한 정도는 필수입니다.")
        ActivityLimitation activityLimitation,

        @Schema(
                description = "불안·우울 정도",
                example = "SOME",
                allowableValues = {"NONE", "SOME", "SEVERE"}
        )
        @NotNull(message = "불안·우울 정도는 필수입니다.")
        AnxietyDepression anxietyDepression,

        @Schema(
                description = "직업군: 14 관리자, 24 전문가·관련종사자, 31 사무, 42 서비스, 52 판매, 61 농림·어업, "
                        + "78 기능원, 87 장치·기계조작·조립, 94 단순노무, none 비경제활동",
                example = "31",
                allowableValues = {"14", "24", "31", "42", "52", "61", "78", "87", "94", "none"}
        )
        @NotNull(message = "직업군은 필수입니다.")
        Occupation occupation
) {
}
