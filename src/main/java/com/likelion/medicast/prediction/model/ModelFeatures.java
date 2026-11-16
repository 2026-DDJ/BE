package com.likelion.medicast.prediction.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ModelFeatures(
        @JsonProperty("ingun") Integer ingun,
        @JsonProperty("income") BigDecimal income,
        @JsonProperty("health_ins") Integer healthInsurance,
        @JsonProperty("region1") Integer region1,
        @JsonProperty("self_rated_health") Integer selfRatedHealth,
        @JsonProperty("height") BigDecimal height,
        @JsonProperty("weight") BigDecimal weight,
        @JsonProperty("exercise") Integer exercise,
        @JsonProperty("ougun") Integer ougun,
        @JsonProperty("ougun_dent") Integer ougunDent,
        @JsonProperty("indemnity_insurance") Integer indemnityInsurance,
        @JsonProperty("hospitalized_year") Integer hospitalizedYear,
        @JsonProperty("activity_limitation") Integer activityLimitation,
        @JsonProperty("anxiety_depression") Integer anxietyDepression,
        @JsonProperty("eco9") Integer eco9
) {

    public ModelFeatures withBodyConditions(BigDecimal simulatedWeight, boolean simulatedExercises) {
        return new ModelFeatures(
                ingun,
                income,
                healthInsurance,
                region1,
                selfRatedHealth,
                height,
                simulatedWeight,
                simulatedExercises ? 1 : 2,
                ougun,
                ougunDent,
                indemnityInsurance,
                hospitalizedYear,
                activityLimitation,
                anxietyDepression,
                eco9
        );
    }
}
