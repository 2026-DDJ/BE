package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.dto.HealthAssessmentInput;
import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.service.HealthAssessmentService;
import com.likelion.medicast.prediction.dto.PredictionResponse;
import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.model.ModelFeatures;
import com.likelion.medicast.prediction.model.PredictionProviderException;
import com.likelion.medicast.prediction.repository.PredictionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private HealthAssessmentService healthAssessmentService;

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private MedicalCostPredictor medicalCostPredictor;

    @Mock
    private ModelFeatureMapper modelFeatureMapper;

    @Test
    void storesFailedPredictionWhenModelIsUnavailable() {
        HealthAssessment assessment = org.mockito.Mockito.mock(HealthAssessment.class);
        HealthAssessmentInput input = org.mockito.Mockito.mock(HealthAssessmentInput.class);
        ModelFeatures modelFeatures = org.mockito.Mockito.mock(ModelFeatures.class);
        when(healthAssessmentService.create(1L, input)).thenReturn(assessment);
        when(modelFeatureMapper.map(assessment)).thenReturn(modelFeatures);
        when(predictionRepository.save(any(Prediction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicalCostPredictor.predict(modelFeatures))
                .thenThrow(new PredictionProviderException("MODEL_UNAVAILABLE", true));

        PredictionService service = new PredictionService(
                healthAssessmentService,
                predictionRepository,
                modelFeatureMapper,
                medicalCostPredictor,
                Clock.fixed(Instant.parse("2026-11-11T01:00:00Z"), ZoneOffset.UTC)
        );

        PredictionResponse response = service.create(1L, input);

        ArgumentCaptor<Prediction> captor = ArgumentCaptor.forClass(Prediction.class);
        verify(predictionRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Prediction saved = captor.getAllValues().get(1);
        assertThat(saved.getStatus()).isEqualTo(PredictionStatus.FAILED);
        assertThat(saved.getErrorCode()).isEqualTo("MODEL_UNAVAILABLE");
        assertThat(response.status()).isEqualTo(PredictionStatus.FAILED);
        assertThat(response.retryable()).isTrue();
    }
}
