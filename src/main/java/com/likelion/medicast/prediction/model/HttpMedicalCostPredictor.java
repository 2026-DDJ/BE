package com.likelion.medicast.prediction.model;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

public class HttpMedicalCostPredictor implements MedicalCostPredictor {

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
                    preprocessingVersion
            );
        } catch (PredictionProviderException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw mapHttpError(exception.getStatusCode());
        } catch (ResourceAccessException exception) {
            throw new PredictionProviderException("MODEL_TIMEOUT", true);
        } catch (RestClientException exception) {
            throw new PredictionProviderException("MODEL_INVALID_RESPONSE", false);
        }
    }

    private PredictionProviderException mapHttpError(HttpStatusCode status) {
        if (status.is4xxClientError()) {
            return new PredictionProviderException("MODEL_REQUEST_REJECTED", false);
        }
        return new PredictionProviderException("MODEL_UNAVAILABLE", true);
    }

    private record AiModelResponse(
            boolean success,
            BigDecimal predictedAnnualMedicalCost,
            BigDecimal highCostProbability
    ) {
    }
}
