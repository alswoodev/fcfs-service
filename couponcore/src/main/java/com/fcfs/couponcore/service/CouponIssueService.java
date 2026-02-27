package com.fcfs.couponcore.service;

import org.springframework.stereotype.Service;

import com.fcfs.couponcore.entity.Coupon;
import com.fcfs.couponcore.entity.CouponIssue;
import com.fcfs.couponcore.exception.CouponIssueException;
import com.fcfs.couponcore.exception.ErrorCode;
import com.fcfs.couponcore.repository.CouponIssueJpaRepository;
import com.fcfs.couponcore.repository.CouponIssueRepository;
import com.fcfs.couponcore.repository.CouponJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CouponIssueService {
    private final CouponIssueRepository couponIssueRepository;
    private final CouponIssueJpaRepository couponIssueJpaRepository;
    private final CouponJpaRepository couponJpaRepository;

    @Transactional
    public void issueCoupon(Long couponId, Long userId) {
        // Find coupon by ID
        Coupon coupon = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXISTS, "Coupon not found with ID: %s".formatted(couponId)));

        coupon.issue();

        CouponIssue couponIssue = CouponIssue.builder()
                .coupon(coupon)
                .userId(userId)
                .build();

        CouponIssue existingCouponIssue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (existingCouponIssue != null) throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE, "You have already been issued coupon user:%s coupon:%s".formatted(userId, couponId));
        couponIssueJpaRepository.save(couponIssue);
    }
}
