package com.likelion.medicast.prediction.controller;

import com.likelion.medicast.assessment.dto.HealthAssessmentInput;
import com.likelion.medicast.auth.security.AuthenticatedUser;
import com.likelion.medicast.global.config.OpenApiConfig;
import com.likelion.medicast.global.exception.ErrorResponse;
import com.likelion.medicast.prediction.dto.PredictionResponse;
import com.likelion.medicast.prediction.dto.SimulationRequest;
import com.likelion.medicast.prediction.dto.SimulationResponse;
import com.likelion.medicast.prediction.service.PredictionService;
import com.likelion.medicast.prediction.service.PredictionSimulationService;
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
    private final PredictionSimulationService predictionSimulationService;

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

    @PostMapping("/predictions/{predictionId}/simulations")
    @Operation(
            operationId = "simulatePrediction",
            summary = "BMI·운동 조건별 의료비 비교",
            description = "기존 예측의 다른 조건은 유지하고 BMI와 규칙적 운동 여부만 바꿔 AI 모델을 다시 호출합니다. "
                    + "목표 BMI는 기존 키를 기준으로 몸무게로 환산되며 시뮬레이션 결과는 저장하지 않습니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 전·후 의료비 비교 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "BMI 범위 또는 요청 형식 오류",
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "기준 예측이 완료 상태가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "502",
                    description = "AI 모델의 요청 거부 또는 잘못된 응답",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "AI 모델 timeout 또는 일시적 장애",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public SimulationResponse simulate(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user,
            @Parameter(description = "비교 기준 예측 ID", example = "1") @PathVariable Long predictionId,
            @Valid @RequestBody SimulationRequest request
    ) {
        return predictionSimulationService.simulate(user.id(), predictionId, request);
    }
}
