package com.likelion.medicast.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super("요청한 리소스를 찾을 수 없습니다.");
    }
}
