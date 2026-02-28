package com.fcfs.couponapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fcfs.couponapi.dto.CouponIssueRequest;
import com.fcfs.couponapi.dto.CouponIssueResponse;
import com.fcfs.couponcore.component.DistributeLockExecutor;
import com.fcfs.couponcore.service.CouponIssueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/coupons")
public class CouponIssueController {
    
    @Autowired
    private CouponIssueService couponIssueService;

    @Autowired
    private DistributeLockExecutor distributeLockExecutor;

    @PostMapping
    public CouponIssueResponse issueCoupon(@RequestBody CouponIssueRequest request) {
        /* 1. Use synchronized to prevent concurrent updates
        synchronized (this) {
            couponIssueService.issueCoupon(request.couponId(), request.userId());
        }*/
        
        distributeLockExecutor.execute("coupon_issue_lock:" + request.couponId(), 10000, 10000, () -> {
            couponIssueService.issueCoupon(request.couponId(), request.userId());
        });
        
        return new CouponIssueResponse(true, "Coupon issued successfully");
    }
    
}