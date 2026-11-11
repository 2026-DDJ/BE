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

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Column(name = "feature_schema_version", length = 100)
    private String featureSchemaVersion;

    @Column(name = "preprocessing_version", length = 100)
    private String preprocessingVersion;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(nullable = false)
    private boolean retryable;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Prediction(
            HealthAssessment assessment
    ) {
        this.assessment = assessment;
        this.status = PredictionStatus.PENDING;
    }

    public void complete(
            BigDecimal predictedMedicalCost,
            String modelVersion,
            String featureSchemaVersion,
            String preprocessingVersion,
            LocalDateTime completedAt
    ) {
        ensurePending();
        if (predictedMedicalCost == null || predictedMedicalCost.signum() < 0) {
            throw new IllegalArgumentException("예측 금액은 0 이상이어야 합니다.");
        }
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("모델 버전은 필수입니다.");
        }

        this.predictedMedicalCost = predictedMedicalCost;
        this.modelVersion = modelVersion;
        this.featureSchemaVersion = featureSchemaVersion;
        this.preprocessingVersion = preprocessingVersion;
        this.status = PredictionStatus.COMPLETED;
        this.errorCode = null;
        this.retryable = false;
        this.completedAt = completedAt;
    }

    public void fail(String errorCode, boolean retryable, LocalDateTime completedAt) {
        ensurePending();
        this.predictedMedicalCost = null;
        this.status = PredictionStatus.FAILED;
        this.errorCode = errorCode;
        this.retryable = retryable;
        this.completedAt = completedAt;
    }

    private void ensurePending() {
        if (status != PredictionStatus.PENDING) {
            throw new IllegalStateException("대기 중인 예측만 완료하거나 실패 처리할 수 있습니다.");
        }
    }
}
