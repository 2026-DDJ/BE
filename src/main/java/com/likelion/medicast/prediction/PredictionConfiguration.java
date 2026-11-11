package com.likelion.medicast.prediction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class PredictionConfiguration {

    @Bean
    public Clock predictionClock() {
        return Clock.systemUTC();
    }
}
