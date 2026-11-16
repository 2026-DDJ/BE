package com.likelion.medicast.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "400", readOnly = true)
        int status,

        @Schema(description = "클라이언트가 분기에 사용할 안정적인 오류 코드", example = "VALIDATION_ERROR", readOnly = true)
        String code,

        @Schema(description = "사용자에게 표시 가능한 오류 메시지", example = "요청 값을 확인해 주세요.", readOnly = true)
        String message,

        @Schema(description = "오류 발생 시각(UTC)", example = "2026-11-16T04:00:00", format = "date-time", readOnly = true)
        LocalDateTime timestamp
) {

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, LocalDateTime.now(ZoneOffset.UTC));
    }
}
