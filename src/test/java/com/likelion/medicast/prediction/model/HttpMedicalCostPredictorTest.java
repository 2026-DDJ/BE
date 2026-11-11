package com.likelion.medicast.prediction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class HttpMedicalCostPredictorTest {

    private MockRestServiceServer server;
    private HttpMedicalCostPredictor predictor;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://model.test");
        server = MockRestServiceServer.bindTo(builder).build();
        predictor = new HttpMedicalCostPredictor(
                builder.build(),
                "/predict",
                "xgboost-2026-11-11",
                "khp-2024-v1"
        );
    }

    @Test
    void sendsExactAiPayloadAndReadsResponse() {
        server.expect(once(), requestTo("http://model.test/predict"))
                .andExpect(method(POST))
                .andExpect(content().json("""
                        {
                          "ingun": 3,
                          "income": 3380,
                          "health_ins": 1,
                          "region1": 11,
                          "self_rated_health": 3,
                          "height": 170,
                          "weight": 65,
                          "exercise": 1,
                          "ougun": 4,
                          "ougun_dent": 1,
                          "indemnity_insurance": 1,
                          "hospitalized_year": 0,
                          "activity_limitation": 2,
                          "anxiety_depression": 3,
                          "eco9": 31
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "predictedAnnualMedicalCost": 1234567.89,
                          "highCostProbability": 0.4321
                        }
                        """, MediaType.APPLICATION_JSON));

        PredictionResult result = predictor.predict(request());

        assertThat(result.predictedAnnualMedicalCost()).isEqualByComparingTo("1234567.89");
        assertThat(result.highCostProbability()).isEqualByComparingTo("0.4321");
        assertThat(result.modelVersion()).isEqualTo("xgboost-2026-11-11");
        assertThat(result.featureSchemaVersion()).isEqualTo("notion-15-v1");
        assertThat(result.preprocessingVersion()).isEqualTo("khp-2024-v1");
        server.verify();
    }

    @Test
    void mapsModelClientErrorWithoutRetry() {
        server.expect(requestTo("http://model.test/predict")).andRespond(withBadRequest());

        assertThatThrownBy(() -> predictor.predict(request()))
                .isInstanceOfSatisfying(PredictionProviderException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("MODEL_REQUEST_REJECTED");
                    assertThat(exception.isRetryable()).isFalse();
                });
    }

    @Test
    void mapsModelServerErrorAsRetryable() {
        server.expect(requestTo("http://model.test/predict")).andRespond(withServerError());

        assertThatThrownBy(() -> predictor.predict(request()))
                .isInstanceOfSatisfying(PredictionProviderException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("MODEL_UNAVAILABLE");
                    assertThat(exception.isRetryable()).isTrue();
                });
    }

    @Test
    void mapsTimeoutAsRetryable() {
        server.expect(requestTo("http://model.test/predict"))
                .andRespond(withException(new SocketTimeoutException("timeout")));

        assertThatThrownBy(() -> predictor.predict(request()))
                .isInstanceOfSatisfying(PredictionProviderException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("MODEL_TIMEOUT");
                    assertThat(exception.isRetryable()).isTrue();
                });
    }

    @Test
    void rejectsMalformedModelResponse() {
        server.expect(requestTo("http://model.test/predict"))
                .andRespond(withSuccess("{", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> predictor.predict(request()))
                .isInstanceOfSatisfying(PredictionProviderException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("MODEL_INVALID_RESPONSE");
                    assertThat(exception.isRetryable()).isFalse();
                });
    }

    @Test
    void rejectsUnsuccessfulModelResponse() {
        server.expect(requestTo("http://model.test/predict"))
                .andRespond(withSuccess("""
                        {
                          "success": false,
                          "predictedAnnualMedicalCost": null,
                          "highCostProbability": null
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> predictor.predict(request()))
                .isInstanceOfSatisfying(PredictionProviderException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("MODEL_PREDICTION_FAILED");
                    assertThat(exception.isRetryable()).isFalse();
                });
    }

    private ModelFeatures request() {
        return new ModelFeatures(
                3,
                new BigDecimal("3380"),
                1,
                11,
                3,
                new BigDecimal("170"),
                new BigDecimal("65"),
                1,
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
