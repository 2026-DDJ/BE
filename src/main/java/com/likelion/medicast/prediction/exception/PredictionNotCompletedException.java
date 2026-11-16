package com.likelion.medicast.prediction.exception;

public class PredictionNotCompletedException extends RuntimeException {

    public PredictionNotCompletedException() {
        super("완료된 기존 예측만 시뮬레이션할 수 있습니다.");
    }
}
