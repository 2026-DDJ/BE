package com.likelion.medicast.prediction;

import com.likelion.medicast.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/assessments/{assessmentId}/predictions")
    public ResponseEntity<PredictionResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long assessmentId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predictionService.create(user.id(), assessmentId));
    }

    @GetMapping("/predictions/{predictionId}")
    public PredictionResponse get(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long predictionId
    ) {
        return predictionService.get(user.id(), predictionId);
    }
}
