package com.fcfs.couponapi.dto;


public record CouponIssueRequest(
    Long userId,
    Long couponId
) {
}
