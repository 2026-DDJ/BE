package com.likelion.medicast.prediction.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

public class HttpMedicalCostPredictor implements MedicalCostPredictor {

    private static final Logger log = LoggerFactory.getLogger(HttpMedicalCostPredictor.class);

    private final RestClient restClient;
    private final String predictionPath;
    private final String modelVersion;
    private final String preprocessingVersion;

    public HttpMedicalCostPredictor(
            RestClient restClient,
            String predictionPath,
            String modelVersion,
            String preprocessingVersion
    ) {
        this.restClient = restClient;
        this.predictionPath = predictionPath;
        this.modelVersion = modelVersion;
        this.preprocessingVersion = preprocessingVersion;
    }

    @Override
    public PredictionResult predict(ModelFeatures features) {
        try {
            AiModelResponse response = restClient.post()
                    .uri(predictionPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(features)
                    .retrieve()
                    .body(AiModelResponse.class);
            if (response == null) {
                throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
            }
            if (!response.success()) {
                throw new PredictionProviderException("MODEL_PREDICTION_FAILED", false);
            }
            return new PredictionResult(
                    response.predictedAnnualMedicalCost(),
                    response.highCostProbability(),
                    modelVersion,
                    ModelFeatureMapper.FEATURE_SCHEMA_VERSION,
                    preprocessingVersion,
                    mean(response.comparison() == null ? null : response.comparison().sameIncomeGroup()),
                    mean(response.comparison() == null ? null : response.comparison().sameOutpatientGroup()),
                    mean(response.comparison() == null ? null : response.comparison().overall())
            );
        } catch (PredictionProviderException exception) {
            log.warn(
                    "AI prediction failed: code={}, path={}",
                    exception.getCode(),
                    predictionPath
            );
            throw exception;
        } catch (RestClientResponseException exception) {
            PredictionProviderException mappedException = mapHttpError(exception.getStatusCode());
            log.warn(
                    "AI prediction HTTP request failed: code={}, status={}, path={}",
                    mappedException.getCode(),
                    exception.getStatusCode().value(),
                    predictionPath
            );
            throw mappedException;
        } catch (ResourceAccessException exception) {
            log.warn("AI prediction request failed: code=MODEL_TIMEOUT, path={}", predictionPath);
            throw new PredictionProviderException("MODEL_TIMEOUT", true);
        } catch (RestClientException exception) {
            log.warn(
                    "AI prediction response failed: code=MODEL_INVALID_RESPONSE, path={}, exceptionType={}",
                    predictionPath,
                    exception.getClass().getSimpleName()
            );
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
    }

    private PredictionProviderException mapHttpError(HttpStatusCode status) {
        if (status.is4xxClientError()) {
            return new PredictionProviderException("MODEL_REQUEST_REJECTED", false);
        }
        return new PredictionProviderException("MODEL_UNAVAILABLE", true);
    }

    private static BigDecimal mean(ComparisonGroup group) {
        return group == null ? null : group.mean();
    }

    private record ComparisonGroup(BigDecimal mean) {}

    private record ComparisonData(ComparisonGroup overall,
            ComparisonGroup sameIncomeGroup, ComparisonGroup sameOutpatientGroup) {}

    private record AiModelResponse(
            boolean success,
            BigDecimal predictedAnnualMedicalCost,
            BigDecimal highCostProbability,
            ComparisonData comparison
    ) {
    }
}
