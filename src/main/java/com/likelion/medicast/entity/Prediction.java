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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "predictions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private HealthAssessment assessment;

    @Column(name = "predicted_medical_cost", precision = 15, scale = 2)
    private BigDecimal predictedMedicalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PredictionStatus status;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Prediction(
            HealthAssessment assessment
    ) {
        this.assessment = assessment;
        this.status = PredictionStatus.PENDING;
    }

    public void complete(BigDecimal predictedMedicalCost) {
        this.predictedMedicalCost = predictedMedicalCost;
        this.status = PredictionStatus.COMPLETED;
        this.errorMessage = null;
    }

    public void fail(String errorMessage) {
        this.predictedMedicalCost = null;
        this.status = PredictionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
