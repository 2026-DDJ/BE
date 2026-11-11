package com.likelion.medicast.prediction.model;

public class PredictionProviderException extends RuntimeException {

    private final String code;
    private final boolean retryable;

    public PredictionProviderException(String code, boolean retryable) {
        super(code);
        this.code = code;
        this.retryable = retryable;
    }

    public String getCode() {
        return code;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
