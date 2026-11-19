package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;
import com.likelion.medicast.prediction.exception.PredictionNotCompletedException;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.model.ModelFeatures;
import com.likelion.medicast.prediction.repository.PredictionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionSimulationSnapshotLoaderTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private ModelFeatureMapper modelFeatureMapper;

    @Mock
    private Prediction prediction;

    @Mock
    private HealthAssessment assessment;

    @Test
    void loadsImmutableSnapshotInsideReadOnlyTransactionBoundary() throws NoSuchMethodException {
        ModelFeatures features = mock(ModelFeatures.class);
        when(predictionRepository.findByIdAndAssessment_User_Id(7L, 1L))
                .thenReturn(Optional.of(prediction));
        when(prediction.getStatus()).thenReturn(PredictionStatus.COMPLETED);
        when(prediction.getId()).thenReturn(7L);
        when(prediction.getAssessment()).thenReturn(assessment);
        when(prediction.getPredictedMedicalCost()).thenReturn(new BigDecimal("1000000"));
        when(assessment.getHeightCm()).thenReturn(new BigDecimal("170"));
        when(assessment.getBmi()).thenReturn(new BigDecimal("22.4913"));
        when(assessment.isExercises()).thenReturn(true);
        when(modelFeatureMapper.map(assessment)).thenReturn(features);

        PredictionSimulationSnapshotLoader loader = new PredictionSimulationSnapshotLoader(
                predictionRepository,
                modelFeatureMapper
        );

        PredictionSimulationSnapshot snapshot = loader.load(1L, 7L);
        Transactional transactional = PredictionSimulationSnapshotLoader.class
                .getMethod("load", Long.class, Long.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
        assertThat(snapshot.predictionId()).isEqualTo(7L);
        assertThat(snapshot.heightCm()).isEqualByComparingTo("170");
        assertThat(snapshot.predictedMedicalCost()).isEqualByComparingTo("1000000");
        assertThat(snapshot.modelFeatures()).isSameAs(features);
    }

    @Test
    void rejectsSimulationWhenOriginalPredictionFailed() {
        when(predictionRepository.findByIdAndAssessment_User_Id(7L, 1L))
                .thenReturn(Optional.of(prediction));
        when(prediction.getStatus()).thenReturn(PredictionStatus.FAILED);

        PredictionSimulationSnapshotLoader loader = new PredictionSimulationSnapshotLoader(
                predictionRepository,
                modelFeatureMapper
        );

        assertThatThrownBy(() -> loader.load(1L, 7L))
                .isInstanceOf(PredictionNotCompletedException.class);
    }
}
