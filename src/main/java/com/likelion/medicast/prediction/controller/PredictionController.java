package com.likelion.medicast.prediction.controller;

import com.likelion.medicast.assessment.dto.HealthAssessmentInput;
import com.likelion.medicast.auth.security.AuthenticatedUser;
import com.likelion.medicast.prediction.dto.PredictionResponse;
import com.likelion.medicast.prediction.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/predictions")
    public ResponseEntity<PredictionResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody HealthAssessmentInput input
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predictionService.create(user.id(), input));
    }

    @GetMapping("/predictions/{predictionId}")
    public PredictionResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long predictionId
    ) {
        return predictionService.get(user.id(), predictionId);
    }
}
