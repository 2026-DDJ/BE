package com.likelion.medicast.auth.dto;

import com.likelion.medicast.auth.security.AuthenticatedUser;
import com.likelion.medicast.user.entity.User;

// 사용자 정보 응답
public record UserResponse(
        Long id,
        String email,
        String name
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }

    public static UserResponse from(AuthenticatedUser user) {
        return new UserResponse(user.id(), user.email(), user.name());
    }
}
