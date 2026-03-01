package com.fcfs.couponcore.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcfs.couponcore.component.DistributeLockExecutor;
import com.fcfs.couponcore.entity.Coupon;
import com.fcfs.couponcore.exception.CouponIssueException;
import com.fcfs.couponcore.exception.ErrorCode;
import com.fcfs.couponcore.repository.CouponJpaRepository;
import com.fcfs.couponcore.repository.RedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsyncCouponIssueService {
    private final RedisRepository redisRepository;
    //private final CouponJpaRepository couponJpaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DistributeLockExecutor distributeLockExecutor;
    private final CouponJpaRepository couponJpaRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int COUPON_ISSUE_LIMIT = 500;
    private static final String ISSUE_REQUEST_QUEUE_KEY = "issue.request.queue";

    public void issueCouponWithOrderedSet(Long userId, Long couponId) {
        Coupon coupon = couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXISTS, "Coupon not found with ID: %s".formatted(couponId)));
        String key = "issue.request.sorted_set.couponId=%s".formatted(coupon.getId());
        String value = userId.toString();
        double score = System.currentTimeMillis(); // Use current time as score

        // 1. Check whether the coupon is within the issue period
        if(!coupon.isWithinIssuePeriod()) throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_TIME, "Coupon is not within issue period user:%s coupon:%s".formatted(userId, coupon.getId()));

        // Use distributed lock to ensure that the check and update operations are atomic
        distributeLockExecutor.execute("coupon_issue_lock:" + couponId, 10000, 10000, () -> {
            // 2. Check if the user has already requested the coupon
            if(redisRepository.zScore(key, value)) throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE, "You have already requested coupon user:%s coupon:%s".formatted(userId, couponId));
            
            // 3. Check if the number of requests has exceeded the limit
            Long count = redisRepository.zCard(key);
            if(count >= COUPON_ISSUE_LIMIT) throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "Coupon issue limit exceeded for coupon:%s".formatted(couponId));
            
            // 4. Add the user ID to the sorted set to track that they have requested this coupon
            redisRepository.zAdd(key, value, score);
        });
    }
}
