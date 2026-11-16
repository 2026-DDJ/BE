package com.likelion.medicast.prediction.controller;

import com.likelion.medicast.assessment.dto.HealthAssessmentInput;
import com.likelion.medicast.auth.security.AuthenticatedUser;
import com.likelion.medicast.global.config.OpenApiConfig;
import com.likelion.medicast.global.exception.ErrorResponse;
import com.likelion.medicast.prediction.dto.PredictionResponse;
import com.likelion.medicast.prediction.service.PredictionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "예측", description = "건강·의료이용 입력을 저장하고 예상 연간 의료비를 생성·조회하는 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping("/predictions")
    @Operation(
            operationId = "createPrediction",
            summary = "의료비 예측 생성",
            description = "15개 건강·의료이용 입력을 검증·저장하고 모델을 동기 호출합니다. "
                    + "모델 호출이 실패하면 임의의 비용 대신 FAILED 상태와 안정적인 errorCode를 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "평가 및 예측 생성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "누락, 범위 초과 또는 허용되지 않은 선택값",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보 누락 또는 유효하지 않은 JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PredictionResponse> create(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody HealthAssessmentInput input
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(predictionService.create(user.id(), input));
    }

    @GetMapping("/predictions/{predictionId}")
    @Operation(
            operationId = "getPrediction",
            summary = "의료비 예측 상세 조회",
            description = "인증 사용자가 소유한 예측의 상태와 결과를 조회합니다. "
                    + "다른 사용자의 예측은 존재 여부를 노출하지 않도록 404로 응답합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예측 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 예측 ID 형식",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보 누락 또는 유효하지 않은 JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "예측이 없거나 인증 사용자가 소유하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public PredictionResponse get(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "예측 ID", example = "1") @PathVariable Long predictionId
    ) {
        return predictionService.get(user.id(), predictionId);
    }
}
