package com.likelion.medicast.auth.dto;

import com.likelion.medicast.user.entity.User;

// 회원가입
public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
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
