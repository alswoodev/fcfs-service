package com.fcfs.couponapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fcfs.couponapi.dto.CouponIssueRequest;
import com.fcfs.couponapi.dto.CouponIssueResponse;
import com.fcfs.couponcore.component.DistributeLockExecutor;
import com.fcfs.couponcore.service.AsyncCouponIssueService;
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
    private AsyncCouponIssueService asyncCouponIssueService;

    @Autowired
    private DistributeLockExecutor distributeLockExecutor;

    @PostMapping
    public CouponIssueResponse issueCoupon(@RequestBody CouponIssueRequest request) {
        /* 1. Use synchronized to prevent concurrent updates
        synchronized (this) {
            couponIssueService.issueCoupon(request.couponId(), request.userId());
        }*/
        
        /* 2. Use distributed lock with redis redlock to prevent concurrent updates
        distributeLockExecutor.execute("coupon_issue_lock:" + request.couponId(), 10000, 10000, () -> {
            couponIssueService.issueCoupon(request.couponId(), request.userId());
        });
        */
        
        // 3. Use pessimistic lock with database to prevent concurrent updates
        couponIssueService.issueCoupon(request.couponId(), request.userId());

        return new CouponIssueResponse(true, "Coupon issued successfully");
    }

    @PostMapping("/async")
    public CouponIssueResponse asyncIssueCoupon(@RequestBody CouponIssueRequest request) {
        //asyncCouponIssueService.issueCouponWithOrderedSet(request.userId(), request.couponId());
        //asyncCouponIssueService.issueCouponWithSetAndQueue(request.userId(), request.couponId());
        asyncCouponIssueService.issueCouponWithLuaScript(request.userId(), request.couponId());
        return new CouponIssueResponse(true, "Async coupon issued successfully");
    }
    
    
}