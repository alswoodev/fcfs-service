package com.fcfs.couponapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CouponIssueResponse (
    boolean success,
    String message
) {
    
}
