package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.global.exception.ResourceNotFoundException;
import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;
import com.likelion.medicast.prediction.exception.PredictionNotCompletedException;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.repository.PredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PredictionSimulationSnapshotLoader {

    private final PredictionRepository predictionRepository;
    private final ModelFeatureMapper modelFeatureMapper;

    @Transactional(readOnly = true)
    public PredictionSimulationSnapshot load(Long userId, Long predictionId) {
        Prediction prediction = predictionRepository.findByIdAndAssessment_User_Id(predictionId, userId)
                .orElseThrow(ResourceNotFoundException::new);
        ensureCompleted(prediction);

        HealthAssessment assessment = prediction.getAssessment();
        return new PredictionSimulationSnapshot(
                prediction.getId(),
                assessment.getHeightCm(),
                assessment.getBmi(),
                assessment.isExercises(),
                prediction.getPredictedMedicalCost(),
                modelFeatureMapper.map(assessment)
        );
    }

    private void ensureCompleted(Prediction prediction) {
        if (prediction.getStatus() != PredictionStatus.COMPLETED
                || prediction.getPredictedMedicalCost() == null) {
            throw new PredictionNotCompletedException();
        }
    }
}
