package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.service.BodyMassIndexCalculator;
import com.likelion.medicast.global.exception.ResourceNotFoundException;
import com.likelion.medicast.prediction.dto.SimulationConditionResponse;
import com.likelion.medicast.prediction.dto.SimulationRequest;
import com.likelion.medicast.prediction.dto.SimulationResponse;
import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;
import com.likelion.medicast.prediction.exception.PredictionNotCompletedException;
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

@Service
@RequiredArgsConstructor
public class PredictionSimulationService {

    private static final String CURRENCY = "KRW";

    private final PredictionRepository predictionRepository;
    private final ModelFeatureMapper modelFeatureMapper;
    private final MedicalCostPredictor medicalCostPredictor;
    private final BodyMassIndexCalculator bodyMassIndexCalculator;

    @Transactional(readOnly = true)
    public SimulationResponse simulate(Long userId, Long predictionId, SimulationRequest request) {
        Prediction prediction = predictionRepository.findByIdAndAssessment_User_Id(predictionId, userId)
                .orElseThrow(ResourceNotFoundException::new);
        ensureCompleted(prediction);

        HealthAssessment assessment = prediction.getAssessment();
        BigDecimal simulatedWeight = bodyMassIndexCalculator.weightForBmi(
                assessment.getHeightCm(),
                request.bmi()
        );
        ModelFeatures simulatedFeatures = modelFeatureMapper.map(assessment)
                .withBodyConditions(simulatedWeight, request.exercises());
        PredictionResult simulatedResult = medicalCostPredictor.predict(simulatedFeatures);
        validate(simulatedResult);

        SimulationConditionResponse before = new SimulationConditionResponse(
                assessment.getBmi(),
                assessment.isExercises(),
                prediction.getPredictedMedicalCost(),
                prediction.getHighCostProbability()
        );
        SimulationConditionResponse after = new SimulationConditionResponse(
                request.bmi(),
                request.exercises(),
                simulatedResult.predictedAnnualMedicalCost(),
                simulatedResult.highCostProbability()
        );

        return new SimulationResponse(
                prediction.getId(),
                before,
                after,
                simulatedResult.predictedAnnualMedicalCost().subtract(prediction.getPredictedMedicalCost()),
                CURRENCY,
                simulatedResult.modelVersion()
        );
    }

    private void ensureCompleted(Prediction prediction) {
        if (prediction.getStatus() != PredictionStatus.COMPLETED
                || prediction.getPredictedMedicalCost() == null
                || prediction.getHighCostProbability() == null) {
            throw new PredictionNotCompletedException();
        }
    }

    private void validate(PredictionResult result) {
        if (result == null
                || result.predictedAnnualMedicalCost() == null
                || result.predictedAnnualMedicalCost().signum() < 0
                || result.highCostProbability() == null
                || result.highCostProbability().compareTo(BigDecimal.ZERO) < 0
                || result.highCostProbability().compareTo(BigDecimal.ONE) > 0
                || result.modelVersion() == null
                || result.modelVersion().isBlank()
                || result.featureSchemaVersion() == null
                || !ModelFeatureMapper.FEATURE_SCHEMA_VERSION.equals(result.featureSchemaVersion())
                || result.preprocessingVersion() == null
                || result.preprocessingVersion().isBlank()) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
    }
}
