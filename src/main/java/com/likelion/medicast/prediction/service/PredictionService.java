package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.dto.HealthAssessmentInput;
import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.service.HealthAssessmentService;
import com.likelion.medicast.global.exception.ResourceNotFoundException;
import com.likelion.medicast.prediction.dto.PredictionResponse;
import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.model.ModelFeatures;
import com.likelion.medicast.prediction.model.PredictionProviderException;
import com.likelion.medicast.prediction.model.PredictionResult;
import com.likelion.medicast.prediction.repository.PredictionRepository;
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

    private final HealthAssessmentService healthAssessmentService;
    private final PredictionRepository predictionRepository;
    private final ModelFeatureMapper modelFeatureMapper;
    private final MedicalCostPredictor medicalCostPredictor;
    private final Clock predictionClock;

    @Transactional
    public PredictionResponse create(Long userId, HealthAssessmentInput input) {
        HealthAssessment assessment = healthAssessmentService.create(userId, input);
        Prediction prediction = predictionRepository.save(new Prediction(assessment));
        ModelFeatures modelFeatures = modelFeatureMapper.map(assessment);

        try {
            PredictionResult result = medicalCostPredictor.predict(modelFeatures);
            validate(result);
            prediction.complete(
                    result.predictedAnnualMedicalCost(),
                    result.highCostProbability(),
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
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
        BigDecimal cost = result.predictedAnnualMedicalCost();
        if (cost == null || cost.signum() < 0) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
        BigDecimal highCostProbability = result.highCostProbability();
        if (highCostProbability == null
                || highCostProbability.compareTo(BigDecimal.ZERO) < 0
                || highCostProbability.compareTo(BigDecimal.ONE) > 0) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
        if (result.modelVersion() == null || result.modelVersion().isBlank()) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
        if (result.featureSchemaVersion() == null || result.featureSchemaVersion().isBlank()) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
        if (!ModelFeatureMapper.FEATURE_SCHEMA_VERSION.equals(result.featureSchemaVersion())) {
            throw new PredictionProviderException("MODEL_SCHEMA_MISMATCH", false);
        }
        if (result.preprocessingVersion() == null || result.preprocessingVersion().isBlank()) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.ofInstant(predictionClock.instant(), ZoneOffset.UTC);
    }
}
