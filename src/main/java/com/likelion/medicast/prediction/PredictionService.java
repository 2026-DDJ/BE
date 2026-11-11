package com.likelion.medicast.prediction;

import com.likelion.medicast.entity.HealthAssessment;
import com.likelion.medicast.entity.Prediction;
import com.likelion.medicast.exception.ResourceNotFoundException;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.PredictionProviderException;
import com.likelion.medicast.prediction.model.PredictionResult;
import com.likelion.medicast.repository.HealthAssessmentRepository;
import com.likelion.medicast.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PredictionService {

    private final HealthAssessmentRepository healthAssessmentRepository;
    private final PredictionRepository predictionRepository;
    private final MedicalCostPredictor medicalCostPredictor;
    private final Clock predictionClock;

    @Transactional
    public PredictionResponse create(Long userId, Long assessmentId) {
        HealthAssessment assessment = healthAssessmentRepository.findByIdAndUser_Id(assessmentId, userId)
                .orElseThrow(ResourceNotFoundException::new);
        Prediction prediction = predictionRepository.save(new Prediction(assessment));

        try {
            PredictionResult result = medicalCostPredictor.predict(assessment);
            validate(result);
            prediction.complete(
                    result.predictedAnnualMedicalCost(),
                    result.modelVersion(),
                    result.featureSchemaVersion(),
                    result.preprocessingVersion(),
                    now()
            );
        } catch (PredictionProviderException exception) {
            prediction.fail(exception.getCode(), exception.isRetryable(), now());
        } catch (RuntimeException exception) {
            prediction.fail("PREDICTION_FAILED", false, now());
        }

        return PredictionResponse.from(predictionRepository.save(prediction));
    }

    @Transactional(readOnly = true)
    public PredictionResponse get(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndAssessment_User_Id(predictionId, userId)
                .orElseThrow(ResourceNotFoundException::new);
        return PredictionResponse.from(prediction);
    }

    private void validate(PredictionResult result) {
        if (result == null) {
            throw new IllegalArgumentException("예측 결과가 없습니다.");
        }
        BigDecimal cost = result.predictedAnnualMedicalCost();
        if (cost == null || cost.signum() < 0) {
            throw new IllegalArgumentException("예측 금액이 유효하지 않습니다.");
        }
        if (result.modelVersion() == null || result.modelVersion().isBlank()) {
            throw new IllegalArgumentException("모델 버전이 없습니다.");
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(predictionClock.instant(), ZoneOffset.UTC);
    }
}
