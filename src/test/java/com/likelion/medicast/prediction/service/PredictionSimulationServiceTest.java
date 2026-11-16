package com.likelion.medicast.prediction.service;

import com.likelion.medicast.assessment.service.BodyMassIndexCalculator;
import com.likelion.medicast.prediction.dto.SimulationRequest;
import com.likelion.medicast.prediction.dto.SimulationResponse;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import com.likelion.medicast.prediction.model.ModelFeatureMapper;
import com.likelion.medicast.prediction.model.ModelFeatures;
import com.likelion.medicast.prediction.model.PredictionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionSimulationServiceTest {

    @Mock
    private PredictionSimulationSnapshotLoader snapshotLoader;

    @Mock
    private MedicalCostPredictor medicalCostPredictor;

    @Test
    void changesOnlyWeightDerivedFromBmiAndExercise() {
        ModelFeatures originalFeatures = features(new BigDecimal("65"), 1);
        when(snapshotLoader.load(1L, 7L)).thenReturn(new PredictionSimulationSnapshot(
                7L,
                new BigDecimal("170"),
                new BigDecimal("22.4913"),
                true,
                new BigDecimal("1000000"),
                new BigDecimal("0.20"),
                originalFeatures
        ));
        when(medicalCostPredictor.predict(org.mockito.ArgumentMatchers.any(ModelFeatures.class)))
                .thenReturn(new PredictionResult(
                        new BigDecimal("900000"),
                        new BigDecimal("0.10"),
                        "external-model-v1",
                        ModelFeatureMapper.FEATURE_SCHEMA_VERSION,
                        "external-preprocessing-v1"
                ));

        PredictionSimulationService service = new PredictionSimulationService(
                snapshotLoader,
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
    void doesNotWrapAiCallInServiceTransaction() throws NoSuchMethodException {
        boolean transactional = PredictionSimulationService.class
                .getMethod("simulate", Long.class, Long.class, SimulationRequest.class)
                .isAnnotationPresent(Transactional.class);

        assertThat(transactional).isFalse();
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
