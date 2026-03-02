package com.fcfs.couponcore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.fcfs.couponcore.entity.Coupon;
import com.fcfs.couponcore.entity.CouponCompletedEvent;
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
    private final RedisTemplate<String, Object> redisTemplate;

    public final static String LISTENER_CHANNEL_NAME = "couponCompleted";

    @Autowired
    @Lazy
    private CouponIssueService couponIssueService;

    @Cacheable(value = "couponCache", key = "#p0")
    public Coupon getCoupon(Long couponId) {
        //System.out.println("Redis cache missing for couponId: " + couponId);
        return couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXISTS, "Coupon not found with ID: %s".formatted(couponId)));
    }

    @Cacheable(value = "couponCache", key = "#p0", cacheManager="localCacheManager")
    public Coupon getCouponWithLocalCache(Long couponId) {
        //System.out.println("Local cache missing for couponId: " + couponId);
        return couponIssueService.getCoupon(couponId);
    }

    @CachePut(value = "couponCache", key = "#p0")
    public Coupon updateCouponCache(Long couponId) {
        //System.out.println("Change redis cache for couponId: " + couponId);
        return couponJpaRepository.findById(couponId)
                .orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXISTS, "Coupon not found with ID: %s".formatted(couponId)));
    }

    @CachePut(value = "couponCache", key = "#p0", cacheManager="localCacheManager")
    public Coupon updateCouponCacheWithLocalCache(Long couponId) {
        //System.out.println("Change local cache for couponId: " + couponId);
        return couponIssueService.getCoupon(couponId);
    }


    @Transactional
    public void issueCoupon(Long couponId, Long userId) {
        // Find coupon by ID
        Coupon coupon = couponJpaRepository.findByIdForUpdate(couponId)
                .orElseThrow(() -> new CouponIssueException(ErrorCode.COUPON_NOT_EXISTS, "Coupon not found with ID: %s".formatted(couponId)));

        coupon.issue();

        CouponIssue couponIssue = CouponIssue.builder()
                .coupon(coupon)
                .userId(userId)
                .build();

        CouponIssue existingCouponIssue = couponIssueRepository.findFirstCouponIssue(couponId, userId);
        if (existingCouponIssue != null) throw new CouponIssueException(ErrorCode.DUPLICATE_COUPON_ISSUE, "You have already been issued coupon user:%s coupon:%s".formatted(userId, couponId));
        couponIssueJpaRepository.save(couponIssue);

        if (!coupon.isAvailableForIssueWithIssuePeriod()) {
            //System.out.println("Publishing CouponCompletedEvent for couponId: " + couponId);
            publishEvent(new CouponCompletedEvent(couponId));
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    protected void publishEvent(CouponCompletedEvent event) {
        try{
            redisTemplate.convertAndSend(LISTENER_CHANNEL_NAME, event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize CouponCompletedEvent", e);
        }
    }
}
