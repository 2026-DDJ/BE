package com.likelion.medicast.auth.dto;

import com.likelion.medicast.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

// 회원가입
public record AuthResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...", readOnly = true)
        String accessToken,

        @Schema(description = "Authorization header에 사용할 token type", example = "Bearer", readOnly = true)
        String tokenType,

        @Schema(description = "access token 만료까지 남은 초", example = "3600", readOnly = true)
        long expiresIn,

        @Schema(description = "인증된 사용자", readOnly = true)
        UserResponse user
) {

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    public static AuthResponse of(String accessToken, long expiresIn, User user) {
        return new AuthResponse(
                accessToken,
                BEARER_TOKEN_TYPE,
                expiresIn,
                UserResponse.from(user)
        );
    }
}
