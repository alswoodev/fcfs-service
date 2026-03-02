package com.fcfs.couponconsumer.component; 

import org.springframework.scheduling.annotation.EnableScheduling; 
import org.springframework.scheduling.annotation.Scheduled; 
import org.springframework.stereotype.Component; 
import com.fasterxml.jackson.databind.ObjectMapper;
import  com.fcfs.couponcore.entity.RedisCouponQueue; 
import com.fcfs.couponcore.repository.RedisRepository; 
import com.fcfs.couponcore.service.AsyncCouponIssueService; 
import com.fcfs.couponcore.service.CouponIssueService; 
import lombok.RequiredArgsConstructor; 
import lombok.extern.slf4j.Slf4j; 

@Component 
@EnableScheduling 
@RequiredArgsConstructor 
@Slf4j public class CouponIssueListener { 
    private final RedisRepository redisRepository; 
    private final CouponIssueService couponIssueService; 
    private final ObjectMapper objectMapper = new ObjectMapper(); 
    
    @Scheduled(fixedDelay = 1000L) public void listen() { 
        String queueKey = AsyncCouponIssueService.ISSUE_REQUEST_QUEUE_KEY; 
        while (redisRepository.lLen(queueKey) > 0) { 
            try{ 
                RedisCouponQueue target = objectMapper.readValue(redisRepository.lPop(queueKey), RedisCouponQueue.class); 
                if (target != null) { 
                    couponIssueService.issueCoupon(target.couponId(), target.userId()); 
                    log.info("Processed coupon issue request from queue for userId: {}, couponId: {}", target.userId(), target.couponId());
                } 
            } catch(Exception e){ 
                log.error("Failed to process coupon issue request from queue", e); continue; 
            } 
        } 
    } 
}