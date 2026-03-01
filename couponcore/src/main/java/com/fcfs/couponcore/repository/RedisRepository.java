package com.fcfs.couponcore.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RedisRepository {
    private final RedisTemplate<String, String> redisTemplate; //Both key-value types are String

    public Boolean zScore(String key, String value) {
        Double score = redisTemplate.opsForZSet().score(key, value);
        return score != null;
    }

    public Long zCard(String key){
        Long size = redisTemplate.opsForZSet().zCard(key);
        return size != null ? size : 0L;
    }

    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(key, value, score);
    }
}
