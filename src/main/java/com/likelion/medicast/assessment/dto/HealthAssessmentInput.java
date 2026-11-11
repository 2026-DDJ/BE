package com.likelion.medicast.assessment.dto;

import com.likelion.medicast.assessment.entity.ActivityLimitation;
import com.likelion.medicast.assessment.entity.AnxietyDepression;
import com.likelion.medicast.assessment.entity.HealthInsuranceType;
import com.likelion.medicast.assessment.entity.IncomeRange;
import com.likelion.medicast.assessment.entity.Occupation;
import com.likelion.medicast.assessment.entity.Region;
import com.likelion.medicast.assessment.entity.SelfRatedHealth;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record HealthAssessmentInput(
        @NotNull(message = "가구원 수는 필수입니다.")
        @Min(value = 1, message = "가구원 수는 1명 이상이어야 합니다.")
        @Max(value = 10, message = "가구원 수는 10명 이하여야 합니다.")
        Integer householdSize,

        @NotNull(message = "소득 구간은 필수입니다.")
        IncomeRange incomeRange,

        @NotNull(message = "건강보험 유형은 필수입니다.")
        HealthInsuranceType healthInsuranceType,

        @NotNull(message = "거주 지역은 필수입니다.")
        Region region,

        @NotNull(message = "주관적 건강상태는 필수입니다.")
        SelfRatedHealth selfRatedHealth,

        @NotNull(message = "키는 필수입니다.")
        @DecimalMin(value = "0", inclusive = false, message = "키는 0보다 커야 합니다.")
        BigDecimal heightCm,

        @NotNull(message = "몸무게는 필수입니다.")
        @DecimalMin(value = "0", inclusive = false, message = "몸무게는 0보다 커야 합니다.")
        BigDecimal weightKg,

        @NotNull(message = "운동 여부는 필수입니다.")
        Boolean exercises,

        @NotNull(message = "최근 1년 외래 이용 횟수는 필수입니다.")
        @PositiveOrZero(message = "최근 1년 외래 이용 횟수는 0 이상이어야 합니다.")
        Integer outpatientVisitsLastYear,

        @NotNull(message = "최근 1년 치과 이용 횟수는 필수입니다.")
        @PositiveOrZero(message = "최근 1년 치과 이용 횟수는 0 이상이어야 합니다.")
        Integer dentalVisitsLastYear,

        @NotNull(message = "실손보험 가입 여부는 필수입니다.")
        Boolean hasPrivateHealthInsurance,

        @NotNull(message = "최근 1년 입원 여부는 필수입니다.")
        Boolean hospitalizedLastYear,

        @NotNull(message = "활동 제한 정도는 필수입니다.")
        ActivityLimitation activityLimitation,

        @NotNull(message = "불안·우울 정도는 필수입니다.")
        AnxietyDepression anxietyDepression,

        @NotNull(message = "직업군은 필수입니다.")
        Occupation occupation
) {
}
