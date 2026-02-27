package com.fcfs.couponcore.exception;

public class CouponIssueException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;

    public CouponIssueException(ErrorCode errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return "[%s] %s".formatted(errorCode, message);
    }
}