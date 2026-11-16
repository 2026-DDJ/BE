package com.likelion.medicast.auth.dto;

import com.likelion.medicast.auth.security.AuthenticatedUser;
import com.likelion.medicast.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

// 사용자 정보 응답
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1", readOnly = true)
        Long id,

        @Schema(description = "사용자 이메일", example = "user@example.com", readOnly = true)
        String email,

        @Schema(description = "사용자 이름", example = "홍길동", readOnly = true)
        String name
) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName());
    }

    public static UserResponse from(AuthenticatedUser user) {
        return new UserResponse(user.id(), user.email(), user.name());
    }
}
