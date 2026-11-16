package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.entity.HealthAssessment;
import com.likelion.medicast.assessment.service.BodyMassIndexCalculator;
import com.likelion.medicast.prediction.dto.SimulationRequest;
import com.likelion.medicast.prediction.dto.SimulationResponse;
import com.likelion.medicast.prediction.entity.Prediction;
import com.likelion.medicast.prediction.entity.PredictionStatus;
import com.likelion.medicast.prediction.exception.PredictionNotCompletedException;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.model.ModelFeatures;
import com.likelion.medicast.prediction.model.PredictionResult;
import com.likelion.medicast.prediction.repository.PredictionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionSimulationServiceTest {

    @Mock
    private PredictionRepository predictionRepository;

    @Mock
    private ModelFeatureMapper modelFeatureMapper;

    @Mock
    private MedicalCostPredictor medicalCostPredictor;

    @Mock
    private Prediction prediction;

    @Mock
    private HealthAssessment assessment;

    @Test
    void changesOnlyWeightDerivedFromBmiAndExercise() {
        ModelFeatures originalFeatures = features(new BigDecimal("65"), 1);
        when(predictionRepository.findByIdAndAssessment_User_Id(7L, 1L))
                .thenReturn(Optional.of(prediction));
        when(prediction.getStatus()).thenReturn(PredictionStatus.COMPLETED);
        when(prediction.getId()).thenReturn(7L);
        when(prediction.getAssessment()).thenReturn(assessment);
        when(prediction.getPredictedMedicalCost()).thenReturn(new BigDecimal("1000000"));
        when(prediction.getHighCostProbability()).thenReturn(new BigDecimal("0.20"));
        when(assessment.getHeightCm()).thenReturn(new BigDecimal("170"));
        when(assessment.getBmi()).thenReturn(new BigDecimal("22.4913"));
        when(assessment.isExercises()).thenReturn(true);
        when(modelFeatureMapper.map(assessment)).thenReturn(originalFeatures);
        when(medicalCostPredictor.predict(org.mockito.ArgumentMatchers.any(ModelFeatures.class)))
                .thenReturn(new PredictionResult(
                        new BigDecimal("900000"),
                        new BigDecimal("0.10"),
                        "external-model-v1",
                        ModelFeatureMapper.FEATURE_SCHEMA_VERSION,
                        "external-preprocessing-v1"
                ));

        PredictionSimulationService service = new PredictionSimulationService(
                predictionRepository,
                modelFeatureMapper,
                medicalCostPredictor,
                new BodyMassIndexCalculator()
        );

        SimulationResponse response = service.simulate(
                1L,
                7L,
                new SimulationRequest(new BigDecimal("20"), false)
        );

        ArgumentCaptor<ModelFeatures> captor = ArgumentCaptor.forClass(ModelFeatures.class);
        verify(medicalCostPredictor).predict(captor.capture());
        ModelFeatures sent = captor.getValue();
        assertThat(sent.weight()).isEqualByComparingTo("57.8000");
        assertThat(sent.exercise()).isEqualTo(2);
        assertThat(sent.ingun()).isEqualTo(originalFeatures.ingun());
        assertThat(sent.income()).isEqualByComparingTo(originalFeatures.income());
        assertThat(sent.region1()).isEqualTo(originalFeatures.region1());
        assertThat(response.before().predictedAnnualMedicalCost()).isEqualByComparingTo("1000000");
        assertThat(response.after().predictedAnnualMedicalCost()).isEqualByComparingTo("900000");
        assertThat(response.annualMedicalCostDifference()).isEqualByComparingTo("-100000");
    }

    @Test
    void rejectsSimulationWhenOriginalPredictionFailed() {
        when(predictionRepository.findByIdAndAssessment_User_Id(7L, 1L))
                .thenReturn(Optional.of(prediction));
        when(prediction.getStatus()).thenReturn(PredictionStatus.FAILED);

        PredictionSimulationService service = new PredictionSimulationService(
                predictionRepository,
                modelFeatureMapper,
                medicalCostPredictor,
                new BodyMassIndexCalculator()
        );

        assertThatThrownBy(() -> service.simulate(
                1L,
                7L,
                new SimulationRequest(new BigDecimal("20"), false)
        )).isInstanceOf(PredictionNotCompletedException.class);
    }

    private ModelFeatures features(BigDecimal weight, int exercise) {
        return new ModelFeatures(
                3,
                new BigDecimal("3380"),
                1,
                11,
                3,
                new BigDecimal("170"),
                weight,
                exercise,
                4,
                1,
                1,
                0,
                2,
                3,
                31
        );
    }
}
