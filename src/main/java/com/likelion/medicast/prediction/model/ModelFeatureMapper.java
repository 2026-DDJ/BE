package com.likelion.medicast.prediction.model;

import com.likelion.medicast.assessment.entity.ActivityLimitation;
import com.likelion.medicast.assessment.entity.AnxietyDepression;
import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.entity.HealthInsuranceType;
import com.likelion.medicast.assessment.entity.IncomeRange;
import com.likelion.medicast.assessment.entity.Occupation;
import com.likelion.medicast.assessment.entity.Region;
import com.likelion.medicast.assessment.entity.SelfRatedHealth;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ModelFeatureMapper {

    public static final String FEATURE_SCHEMA_VERSION = "notion-15-v1";

    public ModelFeatures map(HealthAssessment assessment) {
        return new ModelFeatures(
                assessment.getHouseholdSize(),
                income(assessment.getIncomeRange()),
                healthInsurance(assessment.getHealthInsuranceType()),
                region(assessment.getRegion()),
                selfRatedHealth(assessment.getSelfRatedHealth()),
                assessment.getHeightCm(),
                assessment.getWeightKg(),
                assessment.isExercises() ? 1 : 2,
                assessment.getOutpatientVisitsLastYear(),
                assessment.getDentalVisitsLastYear(),
                assessment.isHasPrivateHealthInsurance() ? 1 : 0,
                assessment.isHospitalizedLastYear() ? 1 : 0,
                activityLimitation(assessment.getActivityLimitation()),
                anxietyDepression(assessment.getAnxietyDepression()),
                occupation(assessment.getOccupation())
        );
    }

    private BigDecimal income(IncomeRange value) {
        return switch (value) {
            case UNDER_2400 -> new BigDecimal("1570.5");
            case FROM_2400_TO_4500 -> new BigDecimal("3380");
            case FROM_4500_TO_7500 -> new BigDecimal("5880");
            case OVER_7500 -> new BigDecimal("10160");
        };
    }

    private Integer healthInsurance(HealthInsuranceType value) {
        return switch (value) {
            case EMPLOYEE_INSURED -> 1;
            case EMPLOYEE_DEPENDENT -> 2;
            case LOCAL_HOUSEHOLDER -> 3;
            case LOCAL_MEMBER -> 4;
            case MEDICAL_AID_HOUSEHOLDER -> 5;
            case MEDICAL_AID_MEMBER -> 6;
            case PATRIOT_VETERAN -> 7;
            case UNINSURED -> null;
        };
    }

    private int region(Region value) {
        return switch (value) {
            case SEOUL -> 11;
            case BUSAN -> 26;
            case DAEGU -> 27;
            case INCHEON -> 28;
            case GWANGJU -> 29;
            case DAEJEON -> 30;
            case ULSAN -> 31;
            case SEJONG -> 36;
            case GYEONGGI -> 41;
            case GANGWON -> 42;
            case CHUNGBUK -> 43;
            case CHUNGNAM -> 44;
            case JEONBUK -> 45;
            case JEONNAM -> 46;
            case GYEONGBUK -> 47;
            case GYEONGNAM -> 48;
            case JEJU -> 50;
        };
    }

    private int selfRatedHealth(SelfRatedHealth value) {
        return switch (value) {
            case VERY_GOOD -> 1;
            case GOOD -> 2;
            case FAIR -> 3;
            case POOR -> 4;
            case VERY_POOR -> 5;
        };
    }

    private int activityLimitation(ActivityLimitation value) {
        return switch (value) {
            case NONE -> 1;
            case SOME -> 2;
            case SEVERE -> 3;
        };
    }

    private int anxietyDepression(AnxietyDepression value) {
        return switch (value) {
            case NONE -> 1;
            case SOME -> 2;
            case SEVERE -> 3;
        };
    }

    private Integer occupation(Occupation value) {
        return switch (value) {
            case MANAGER -> 14;
            case PROFESSIONAL -> 24;
            case OFFICE_WORKER -> 31;
            case SERVICE_WORKER -> 42;
            case SALES_WORKER -> 52;
            case AGRICULTURE_FISHERY -> 61;
            case CRAFT_WORKER -> 78;
            case MACHINE_OPERATOR_ASSEMBLER -> 87;
            case ELEMENTARY_WORKER -> 94;
            case NOT_ECONOMICALLY_ACTIVE -> null;
        };
    }
}
