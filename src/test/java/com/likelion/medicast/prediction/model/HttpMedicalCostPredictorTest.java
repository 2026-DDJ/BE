package com.likelion.medicast.prediction.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(OutputCaptureExtension.class)
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
                .andExpect(header("Content-Type", "application/json"))
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
                          "highCostProbability": 0.4321,
                          "comparison": {
                            "sameIncomeGroup": {"count": 100, "mean": 1501650, "median": 769100},
                            "sameOutpatientGroup": {"count": 150, "mean": 1200000, "median": 600000},
                            "overall": {"count": 11770, "mean": 1391111, "median": 687550}
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PredictionResult result = predictor.predict(request());

        assertThat(result.predictedAnnualMedicalCost()).isEqualByComparingTo("1234567.89");
        assertThat(result.highCostProbability()).isEqualByComparingTo("0.4321");
        assertThat(result.sameIncomeGroupAverageAnnualMedicalCost()).isEqualByComparingTo("1501650");
        assertThat(result.sameOutpatientVisitsGroupAverageAnnualMedicalCost()).isEqualByComparingTo("1200000");
        assertThat(result.overallAverageAnnualMedicalCost()).isEqualByComparingTo("1391111");
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
    void mapsModelServerErrorAsRetryableAndLogsSafeDetails(CapturedOutput output) {
        server.expect(requestTo("http://model.test/predict"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"sensitiveDetail\":\"must-not-be-logged\"}"));

        assertThatThrownBy(() -> predictor.predict(request()))
                .isInstanceOfSatisfying(PredictionProviderException.class, exception -> {
                    assertThat(exception.getCode()).isEqualTo("MODEL_UNAVAILABLE");
                    assertThat(exception.isRetryable()).isTrue();
                });
        assertThat(output)
                .contains("AI prediction HTTP request failed")
                .contains("code=MODEL_UNAVAILABLE")
                .contains("status=500")
                .contains("path=/predict")
                .doesNotContain("must-not-be-logged");
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
