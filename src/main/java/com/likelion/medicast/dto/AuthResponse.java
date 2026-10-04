package com.likelion.medicast.dto;

import com.likelion.medicast.entity.User;

// 회원가입
public record AuthResponse(
        String accessToken,
        long expiresIn,
        UserResponse user
) {

    public static AuthResponse of(String accessToken, long expiresIn, User user) {
        return new AuthResponse(
                accessToken,
                expiresIn,
                UserResponse.from(user)
        );
    }
}
