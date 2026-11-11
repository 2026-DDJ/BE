package com.likelion.medicast.global.exception;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record ErrorResponse(
        int status,
        String code,
        String message,
        LocalDateTime timestamp
) {

    public static ErrorResponse of(int status, String code, String message) {
        return new ErrorResponse(status, code, message, LocalDateTime.now(ZoneOffset.UTC));
    }
}
