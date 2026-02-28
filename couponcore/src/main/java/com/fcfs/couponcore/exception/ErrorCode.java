package com.fcfs.couponcore.exception;

public enum ErrorCode {
    INVALID_COUPON_ISSUE_QUANTITY("쿠폰 모두 소진되었습니다."),
    INVALID_COUPON_ISSUE_TIME("쿠폰 사용기간이 아닙니다."),
    COUPON_NOT_EXISTS("쿠폰이 존재하지 않습니다."),
    DUPLICATE_COUPON_ISSUE("이미 발급된 쿠폰입니다.");

    private String message;
    
    ErrorCode(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
}