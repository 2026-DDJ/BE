package com.likelion.medicast.auth.controller;

import com.likelion.medicast.auth.dto.AuthResponse;
import com.likelion.medicast.auth.dto.LoginRequest;
import com.likelion.medicast.auth.dto.SignupRequest;
import com.likelion.medicast.auth.dto.UserResponse;
import com.likelion.medicast.auth.security.AuthenticatedUser;
import com.likelion.medicast.auth.service.AuthService;
import com.likelion.medicast.global.config.OpenApiConfig;
import com.likelion.medicast.global.exception.ErrorResponse;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "인증", description = "회원가입, 로그인, 인증 사용자 조회 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(
            operationId = "signUp",
            summary = "회원가입",
            description = "이메일 중복을 확인하고 계정을 생성한 뒤 JWT access token을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 이메일",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.signup(request));
    }

    @PostMapping("/login")
    @Operation(
            operationId = "login",
            summary = "로그인",
            description = "이메일과 비밀번호를 확인하고 JWT access token을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "입력값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "잘못된 이메일 또는 비밀번호",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/my")
    @Operation(
            operationId = "getMyProfile",
            summary = "내 정보 조회",
            description = "JWT로 인증된 사용자의 최소 프로필을 반환합니다.",
            security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보 누락 또는 유효하지 않은 JWT",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public UserResponse my(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return UserResponse.from(user);
    }
}
