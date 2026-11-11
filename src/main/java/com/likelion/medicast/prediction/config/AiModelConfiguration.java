package com.likelion.medicast.prediction.config;

import com.likelion.medicast.prediction.model.HttpMedicalCostPredictor;
import com.likelion.medicast.prediction.model.MedicalCostPredictor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@Profile("!local & !test")
public class AiModelConfiguration {

    @Bean
    public MedicalCostPredictor medicalCostPredictor(
            @Value("${ai.model.base-url}") String baseUrl,
            @Value("${ai.model.prediction-path}") String predictionPath,
            @Value("${ai.model.connect-timeout}") Duration connectTimeout,
            @Value("${ai.model.read-timeout}") Duration readTimeout,
            @Value("${ai.model.version}") String modelVersion,
            @Value("${ai.model.preprocessing-version}") String preprocessingVersion
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        RestClient restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        return new HttpMedicalCostPredictor(
                restClient,
                predictionPath,
                modelVersion,
                preprocessingVersion
        );
    }
}
