package com.likelion.medicast.prediction.entity;

import com.likelion.medicast.assessment.entity.HealthAssessment;
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

    @Column(name = "same_income_group_mean", precision = 15, scale = 2)
    private BigDecimal sameIncomeGroupMean;

    @Column(name = "same_outpatient_group_mean", precision = 15, scale = 2)
    private BigDecimal sameOutpatientGroupMean;

    @Column(name = "overall_mean", precision = 15, scale = 2)
    private BigDecimal overallMean;

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
            BigDecimal sameIncomeGroupMean,
            BigDecimal sameOutpatientGroupMean,
            BigDecimal overallMean,
            String modelVersion,
            String featureSchemaVersion,
            String preprocessingVersion,
            LocalDateTime completedAt
    ) {
        ensurePending();
        requireNonNegative(predictedMedicalCost, "예측 금액");
        requireNonNegative(sameIncomeGroupMean, "동일 소득 구간 평균");
        requireNonNegative(sameOutpatientGroupMean, "동일 외래 이용 횟수 구간 평균");
        requireNonNegative(overallMean, "전체 평균");
        if (modelVersion == null || modelVersion.isBlank()) {
            throw new IllegalArgumentException("모델 버전은 필수입니다.");
        }

        this.predictedMedicalCost = predictedMedicalCost;
        this.sameIncomeGroupMean = sameIncomeGroupMean;
        this.sameOutpatientGroupMean = sameOutpatientGroupMean;
        this.overallMean = overallMean;
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
        this.sameIncomeGroupMean = null;
        this.sameOutpatientGroupMean = null;
        this.overallMean = null;
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

    private void requireNonNegative(BigDecimal value, String fieldName) {
        if (value == null || value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + "은(는) 0 이상이어야 합니다.");
        }
    }
}
