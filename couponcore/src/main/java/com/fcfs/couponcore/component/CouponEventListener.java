package com.fcfs.couponcore.component;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fcfs.couponcore.entity.CouponCompletedEvent;
import com.fcfs.couponcore.service.CouponIssueService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CouponEventListener implements MessageListener {
    private final CouponIssueService couponIssueService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            CouponCompletedEvent event = (CouponCompletedEvent) redisTemplate.getValueSerializer()
                .deserialize(message.getBody());
            //System.out.println("Handling CouponCompletedEvent for couponId: " + event.getCouponId());
            couponIssueService.updateCouponCache(event.getCouponId());
            couponIssueService.updateCouponCacheWithLocalCache(event.getCouponId());
            //System.out.println("Successfully handled CouponCompletedEvent for couponId: " + event.getCouponId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
