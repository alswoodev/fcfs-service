package com.fcfs.couponcore.service;

import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fcfs.couponcore.component.DistributeLockExecutor;
import com.fcfs.couponcore.entity.Coupon;
import com.fcfs.couponcore.entity.RedisCouponQueue;
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
    private final CouponIssueService couponIssueService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int COUPON_ISSUE_LIMIT = 500;
    public static final String ISSUE_REQUEST_QUEUE_KEY = "issue.request.queue";

    public void issueCouponWithOrderedSet(Long userId, Long couponId) {
        Coupon coupon = couponIssueService.getCouponWithLocalCache(couponId);
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

    public void issueCouponWithSetAndQueue(Long userId, Long couponId) {
        Coupon coupon = couponIssueService.getCouponWithLocalCache(couponId);
        String key = "issue.request.couponId=%s".formatted(coupon.getId());
        String value = userId.toString();

        // 1. Check whether the coupon is within the issue period
        if(!coupon.isWithinIssuePeriod()) throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_TIME, "Coupon is not within issue period user:%s coupon:%s".formatted(userId, coupon.getId()));
        
        // Use distributed lock to ensure that the check and update operations are atomic
        distributeLockExecutor.execute("coupon_issue_lock:" + couponId, 10000, 10000, () -> {
            //  2. Check if the user has already requested the coupon
            if(redisRepository.sIsMember(key, value)) throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE, "You have already requested coupon user:%s coupon:%s".formatted(userId, coupon.getId()));
            
            // 3. Check if the number of requests has exceeded the limit
            Long count = redisRepository.sCard(key);
            if(count >= COUPON_ISSUE_LIMIT) throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "Coupon issue limit exceeded for coupon:%s".formatted(coupon.getId()));
            
            // 4. Add the user ID to the set to track that they have requested this coupon
            redisRepository.sAdd(key, value);

            // 5. Enqueue the issue request for asynchronous processing
            enqueueIssueRequest(userId, couponId);
        });
    }

    public void issueCouponWithLuaScript(Long userId, Long couponId) {
        Coupon coupon = couponIssueService.getCouponWithLocalCache(couponId);
        String setKey = "issue.request.couponId=%s".formatted(coupon.getId());
        RedisCouponQueue request = new RedisCouponQueue(userId, couponId);

        // 1. Check whether the coupon is within the issue period
        if(!coupon.isWithinIssuePeriod()) throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_TIME, "Coupon is not within issue period user:%s coupon:%s".formatted(userId, coupon.getId()));
        try{
            redisTemplate.execute(redisRepository.issueScript(), 
                            List.of(setKey, ISSUE_REQUEST_QUEUE_KEY), 
                            userId.toString(), String.valueOf(COUPON_ISSUE_LIMIT), objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            throw new CouponIssueException(ErrorCode.FAIL_TO_ISSUE_COUPON, "Failed to issue coupon with Lua script user:%s coupon:%s".formatted(userId, coupon.getId()));
        }
    }

    private void enqueueIssueRequest(Long userId, Long couponId) {
        RedisCouponQueue request = new RedisCouponQueue(userId, couponId);
        try {
            String requestJson = objectMapper.writeValueAsString(request);
            redisRepository.rPush(ISSUE_REQUEST_QUEUE_KEY, requestJson);
        } catch (Exception e) {
            throw new CouponIssueException(ErrorCode.FAIL_TO_ISSUE_COUPON, "Failed to enqueue coupon issue request for user:%s coupon:%s".formatted(userId, couponId));
        }
    }
}
