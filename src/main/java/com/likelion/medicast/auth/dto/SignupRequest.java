package com.likelion.medicast.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 회원가입
public record SignupRequest(
        @Schema(description = "로그인에 사용할 이메일", example = "user@example.com", maxLength = 255)
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
        String email,

        @Schema(
                description = "8~72자 비밀번호",
                example = "password123!",
                format = "password",
                minLength = 8,
                maxLength = 72,
                writeOnly = true
        )
        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 72, message = "비밀번호는 8자 이상 72자 이하여야 합니다.")
        String password,

        @Schema(description = "사용자 이름", example = "홍길동", maxLength = 100)
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String name
) {
}
