package com.likelion.medicast.entity;

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

    @Column(nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sex sex;

    @Column(nullable = false, length = 100)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_range", nullable = false, length = 30)
    private IncomeRange incomeRange;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(name = "has_chronic_disease", nullable = false)
    private boolean hasChronicDisease;

    @Enumerated(EnumType.STRING)
    @Column(name = "self_rated_health", nullable = false, length = 20)
    private SelfRatedHealth selfRatedHealth;

    @Enumerated(EnumType.STRING)
    @Column(name = "smoking_status", nullable = false, length = 20)
    private SmokingStatus smokingStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "drinking_frequency", nullable = false, length = 30)
    private DrinkingFrequency drinkingFrequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_frequency", nullable = false, length = 30)
    private ExerciseFrequency exerciseFrequency;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public HealthAssessment(
            User user,
            Integer age,
            Sex sex,
            String region,
            IncomeRange incomeRange,
            BigDecimal bmi,
            boolean hasChronicDisease,
            SelfRatedHealth selfRatedHealth,
            SmokingStatus smokingStatus,
            DrinkingFrequency drinkingFrequency,
            ExerciseFrequency exerciseFrequency
    ) {
        this.user = user;
        this.age = age;
        this.sex = sex;
        this.region = region;
        this.incomeRange = incomeRange;
        this.bmi = bmi;
        this.hasChronicDisease = hasChronicDisease;
        this.selfRatedHealth = selfRatedHealth;
        this.smokingStatus = smokingStatus;
        this.drinkingFrequency = drinkingFrequency;
        this.exerciseFrequency = exerciseFrequency;
    }
}
