package com.fcfs.couponapi.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fcfs.couponapi.dto.CouponIssueResponse;
import com.fcfs.couponcore.exception.CouponIssueException;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponse handleCouponIssueException(CouponIssueException e) {
        return new CouponIssueResponse(false, e.getErrorCode().getMessage());
    }
}
