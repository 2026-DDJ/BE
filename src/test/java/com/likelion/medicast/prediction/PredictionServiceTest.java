package com.likelion.medicast.prediction;

import com.likelion.medicast.entity.HealthAssessment;
import com.likelion.medicast.entity.Prediction;
import com.likelion.medicast.entity.PredictionStatus;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.PredictionProviderException;
import com.likelion.medicast.repository.HealthAssessmentRepository;
import com.likelion.medicast.repository.PredictionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private HealthAssessmentRepository healthAssessmentRepository;

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private MedicalCostPredictor medicalCostPredictor;

    @Test
    void storesFailedPredictionWhenModelIsUnavailable() {
        HealthAssessment assessment = org.mockito.Mockito.mock(HealthAssessment.class);
        when(healthAssessmentRepository.findByIdAndUser_Id(10L, 1L))
                .thenReturn(Optional.of(assessment));
        when(predictionRepository.save(any(Prediction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(medicalCostPredictor.predict(assessment))
                .thenThrow(new PredictionProviderException("MODEL_UNAVAILABLE", true));

        PredictionService service = new PredictionService(
                healthAssessmentRepository,
                predictionRepository,
                medicalCostPredictor,
                Clock.fixed(Instant.parse("2026-11-11T01:00:00Z"), ZoneOffset.UTC)
        );

        PredictionResponse response = service.create(1L, 10L);

        ArgumentCaptor<Prediction> captor = ArgumentCaptor.forClass(Prediction.class);
        verify(predictionRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        Prediction saved = captor.getAllValues().get(1);
        assertThat(saved.getStatus()).isEqualTo(PredictionStatus.FAILED);
        assertThat(saved.getErrorCode()).isEqualTo("MODEL_UNAVAILABLE");
        assertThat(response.status()).isEqualTo(PredictionStatus.FAILED);
        assertThat(response.retryable()).isTrue();
    }
}
