package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.service.BodyMassIndexCalculator;
import com.likelion.medicast.prediction.dto.SimulationConditionResponse;
import com.likelion.medicast.prediction.dto.SimulationRequest;
import com.likelion.medicast.prediction.dto.SimulationResponse;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.model.ModelFeatures;
import com.likelion.medicast.prediction.model.PredictionProviderException;
import com.likelion.medicast.prediction.model.PredictionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PredictionSimulationService {

    private static final String CURRENCY = "KRW";

    private final PredictionSimulationSnapshotLoader snapshotLoader;
    private final MedicalCostPredictor medicalCostPredictor;
    private final BodyMassIndexCalculator bodyMassIndexCalculator;

    public SimulationResponse simulate(Long userId, Long predictionId, SimulationRequest request) {
        PredictionSimulationSnapshot snapshot = snapshotLoader.load(userId, predictionId);
        BigDecimal simulatedWeight = bodyMassIndexCalculator.weightForBmi(
                snapshot.heightCm(),
                request.bmi()
        );
        ModelFeatures simulatedFeatures = snapshot.modelFeatures()
                .withBodyConditions(simulatedWeight, request.exercises());
        PredictionResult simulatedResult = medicalCostPredictor.predict(simulatedFeatures);
        validate(simulatedResult);

        SimulationConditionResponse before = new SimulationConditionResponse(
                snapshot.bmi(),
                snapshot.exercises(),
                snapshot.predictedMedicalCost(),
                snapshot.highCostProbability()
        );
        SimulationConditionResponse after = new SimulationConditionResponse(
                request.bmi(),
                request.exercises(),
                simulatedResult.predictedAnnualMedicalCost(),
                simulatedResult.highCostProbability()
        );

        return new SimulationResponse(
                snapshot.predictionId(),
                before,
                after,
                simulatedResult.predictedAnnualMedicalCost().subtract(snapshot.predictedMedicalCost()),
                CURRENCY,
                simulatedResult.modelVersion()
        );
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
