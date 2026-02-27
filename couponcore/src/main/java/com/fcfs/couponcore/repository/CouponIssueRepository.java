package com.fcfs.couponcore.repository;

import org.springframework.stereotype.Repository;

import com.fcfs.couponcore.entity.CouponIssue;
import com.fcfs.couponcore.entity.QCouponIssue;
import com.querydsl.jpa.JPQLQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CouponIssueRepository {
    private final JPQLQueryFactory queryFactory;

    public CouponIssue findFirstCouponIssue(Long couponId, Long userId) {
        return queryFactory.selectFrom(QCouponIssue.couponIssue)
                .where(QCouponIssue.couponIssue.coupon.id.eq(couponId)
                        .and(QCouponIssue.couponIssue.userId.eq(userId)))
                .fetchFirst();
    }
}