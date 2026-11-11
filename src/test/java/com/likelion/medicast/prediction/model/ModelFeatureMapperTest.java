package com.likelion.medicast.prediction.model;

import com.likelion.medicast.assessment.entity.ActivityLimitation;
import com.likelion.medicast.assessment.entity.AnxietyDepression;
import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.entity.HealthInsuranceType;
import com.likelion.medicast.assessment.entity.IncomeRange;
import com.likelion.medicast.assessment.entity.Occupation;
import com.likelion.medicast.assessment.entity.Region;
import com.likelion.medicast.assessment.entity.SelfRatedHealth;
import com.likelion.medicast.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ModelFeatureMapperTest {

    private final ModelFeatureMapper mapper = new ModelFeatureMapper();

    @Test
    void mapsLatestAssessmentToVersionedAiFeatures() {
        ModelFeatures features = mapper.map(assessment(
                IncomeRange.FROM_2400_TO_4500,
                HealthInsuranceType.EMPLOYEE_INSURED,
                Region.SEOUL,
                SelfRatedHealth.FAIR,
                ActivityLimitation.SOME,
                AnxietyDepression.SEVERE,
                Occupation.OFFICE_WORKER
        ));

        assertThat(features.ingun()).isEqualTo(3);
        assertThat(features.income()).isEqualByComparingTo("3380");
        assertThat(features.healthInsurance()).isEqualTo(1);
        assertThat(features.region1()).isEqualTo(11);
        assertThat(features.selfRatedHealth()).isEqualTo(3);
        assertThat(features.height()).isEqualByComparingTo("170");
        assertThat(features.weight()).isEqualByComparingTo("65");
        assertThat(features.exercise()).isEqualTo(1);
        assertThat(features.ougun()).isEqualTo(4);
        assertThat(features.ougunDent()).isEqualTo(1);
        assertThat(features.indemnityInsurance()).isEqualTo(1);
        assertThat(features.hospitalizedYear()).isZero();
        assertThat(features.activityLimitation()).isEqualTo(2);
        assertThat(features.anxietyDepression()).isEqualTo(3);
        assertThat(features.eco9()).isEqualTo(31);
    }

    @Test
    void mapsEveryCategoricalValueWithoutFallbacks() {
        BigDecimal[] incomes = {
                new BigDecimal("1570.5"),
                new BigDecimal("3380"),
                new BigDecimal("5880"),
                new BigDecimal("10160")
        };
        for (int index = 0; index < IncomeRange.values().length; index++) {
            ModelFeatures features = mapper.map(assessment(
                    IncomeRange.values()[index], HealthInsuranceType.EMPLOYEE_INSURED,
                    Region.SEOUL, SelfRatedHealth.VERY_GOOD, ActivityLimitation.NONE,
                    AnxietyDepression.NONE, Occupation.MANAGER));
            assertThat(features.income()).isEqualByComparingTo(incomes[index]);
        }

        Integer[] insuranceCodes = {1, 2, 3, 4, 5, 6, 7, null};
        for (int index = 0; index < HealthInsuranceType.values().length; index++) {
            ModelFeatures features = mapper.map(assessment(
                    IncomeRange.UNDER_2400, HealthInsuranceType.values()[index],
                    Region.SEOUL, SelfRatedHealth.VERY_GOOD, ActivityLimitation.NONE,
                    AnxietyDepression.NONE, Occupation.MANAGER));
            assertThat(features.healthInsurance()).isEqualTo(insuranceCodes[index]);
        }

        int[] regionCodes = {11, 26, 27, 28, 29, 30, 31, 36, 41, 42, 43, 44, 45, 46, 47, 48, 50};
        for (int index = 0; index < Region.values().length; index++) {
            ModelFeatures features = mapper.map(assessment(
                    IncomeRange.UNDER_2400, HealthInsuranceType.EMPLOYEE_INSURED,
                    Region.values()[index], SelfRatedHealth.VERY_GOOD, ActivityLimitation.NONE,
                    AnxietyDepression.NONE, Occupation.MANAGER));
            assertThat(features.region1()).isEqualTo(regionCodes[index]);
        }

        for (int index = 0; index < SelfRatedHealth.values().length; index++) {
            ModelFeatures features = mapper.map(assessment(
                    IncomeRange.UNDER_2400, HealthInsuranceType.EMPLOYEE_INSURED,
                    Region.SEOUL, SelfRatedHealth.values()[index], ActivityLimitation.NONE,
                    AnxietyDepression.NONE, Occupation.MANAGER));
            assertThat(features.selfRatedHealth()).isEqualTo(index + 1);
        }

        for (int index = 0; index < ActivityLimitation.values().length; index++) {
            ModelFeatures features = mapper.map(assessment(
                    IncomeRange.UNDER_2400, HealthInsuranceType.EMPLOYEE_INSURED,
                    Region.SEOUL, SelfRatedHealth.VERY_GOOD, ActivityLimitation.values()[index],
                    AnxietyDepression.values()[index], Occupation.MANAGER));
            assertThat(features.activityLimitation()).isEqualTo(index + 1);
            assertThat(features.anxietyDepression()).isEqualTo(index + 1);
        }

        Integer[] occupationCodes = {14, 24, 31, 42, 52, 61, 78, 87, 94, null};
        for (int index = 0; index < Occupation.values().length; index++) {
            ModelFeatures features = mapper.map(assessment(
                    IncomeRange.UNDER_2400, HealthInsuranceType.EMPLOYEE_INSURED,
                    Region.SEOUL, SelfRatedHealth.VERY_GOOD, ActivityLimitation.NONE,
                    AnxietyDepression.NONE, Occupation.values()[index]));
            assertThat(features.eco9()).isEqualTo(occupationCodes[index]);
        }
    }

    @Test
    void mapsBooleanAlternativesToModelCodes() {
        HealthAssessment assessment = new HealthAssessment(
                mock(User.class),
                3,
                IncomeRange.UNDER_2400,
                HealthInsuranceType.UNINSURED,
                Region.JEJU,
                SelfRatedHealth.VERY_GOOD,
                new BigDecimal("170"),
                new BigDecimal("65"),
                new BigDecimal("22.4913"),
                false,
                0,
                0,
                false,
                true,
                ActivityLimitation.NONE,
                AnxietyDepression.NONE,
                Occupation.NOT_ECONOMICALLY_ACTIVE
        );

        ModelFeatures features = mapper.map(assessment);

        assertThat(features.exercise()).isEqualTo(2);
        assertThat(features.indemnityInsurance()).isZero();
        assertThat(features.hospitalizedYear()).isEqualTo(1);
        assertThat(features.healthInsurance()).isNull();
        assertThat(features.eco9()).isNull();
    }

    private HealthAssessment assessment(
            IncomeRange incomeRange,
            HealthInsuranceType healthInsuranceType,
            Region region,
            SelfRatedHealth selfRatedHealth,
            ActivityLimitation activityLimitation,
            AnxietyDepression anxietyDepression,
            Occupation occupation
    ) {
        return new HealthAssessment(
                mock(User.class),
                3,
                incomeRange,
                healthInsuranceType,
                region,
                selfRatedHealth,
                new BigDecimal("170"),
                new BigDecimal("65"),
                new BigDecimal("22.4913"),
                true,
                4,
                1,
                true,
                false,
                activityLimitation,
                anxietyDepression,
                occupation
        );
    }
}
