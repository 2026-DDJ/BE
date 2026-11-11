package com.likelion.medicast.assessment.entity;

import com.likelion.medicast.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "health_assessments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "household_size", nullable = false)
    private Integer householdSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_range", nullable = false, length = 30)
    private IncomeRange incomeRange;

    @Enumerated(EnumType.STRING)
    @Column(name = "health_insurance_type", nullable = false, length = 40)
    private HealthInsuranceType healthInsuranceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(name = "self_rated_health", nullable = false, length = 20)
    private SelfRatedHealth selfRatedHealth;

    @Column(name = "height_cm", nullable = false, precision = 10, scale = 4)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", nullable = false, precision = 10, scale = 4)
    private BigDecimal weightKg;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal bmi;

    @Column(nullable = false)
    private boolean exercises;

    @Column(name = "outpatient_visits_last_year", nullable = false)
    private Integer outpatientVisitsLastYear;

    @Column(name = "dental_visits_last_year", nullable = false)
    private Integer dentalVisitsLastYear;

    @Column(name = "has_private_health_insurance", nullable = false)
    private boolean hasPrivateHealthInsurance;

    @Column(name = "hospitalized_last_year", nullable = false)
    private boolean hospitalizedLastYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_limitation", nullable = false, length = 20)
    private ActivityLimitation activityLimitation;

    @Enumerated(EnumType.STRING)
    @Column(name = "anxiety_depression", nullable = false, length = 20)
    private AnxietyDepression anxietyDepression;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Occupation occupation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public HealthAssessment(
            User user,
            Integer householdSize,
            IncomeRange incomeRange,
            HealthInsuranceType healthInsuranceType,
            Region region,
            SelfRatedHealth selfRatedHealth,
            BigDecimal heightCm,
            BigDecimal weightKg,
            BigDecimal bmi,
            boolean exercises,
            Integer outpatientVisitsLastYear,
            Integer dentalVisitsLastYear,
            boolean hasPrivateHealthInsurance,
            boolean hospitalizedLastYear,
            ActivityLimitation activityLimitation,
            AnxietyDepression anxietyDepression,
            Occupation occupation
    ) {
        this.user = user;
        this.householdSize = householdSize;
        this.incomeRange = incomeRange;
        this.healthInsuranceType = healthInsuranceType;
        this.region = region;
        this.selfRatedHealth = selfRatedHealth;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.bmi = bmi;
        this.exercises = exercises;
        this.outpatientVisitsLastYear = outpatientVisitsLastYear;
        this.dentalVisitsLastYear = dentalVisitsLastYear;
        this.hasPrivateHealthInsurance = hasPrivateHealthInsurance;
        this.hospitalizedLastYear = hospitalizedLastYear;
        this.activityLimitation = activityLimitation;
        this.anxietyDepression = anxietyDepression;
        this.occupation = occupation;
    }
}
