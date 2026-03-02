package com.fcfs.couponcore.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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

    public Boolean sIsMember(String key, String value) {
        Boolean isMember = redisTemplate.opsForSet().isMember(key, value);
        return isMember != null && isMember;
    }

    public Long sCard(String key){
        Long size = redisTemplate.opsForSet().size(key);
        return size != null ? size : 0L;
    }

    public Long sAdd(String key, String value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    //Queue operations
    public Boolean rPush(String key, String value) {
        Long result = redisTemplate.opsForList().rightPush(key, value);
        return result != null && result > 0;
    }

    public String lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    public Long lLen(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size != null ? size : 0L;
    }

    public RedisScript<String> issueScript() {
        String script = """
            local setKey = KEYS[1]
            local queueKey = KEYS[2]
            local userId = ARGV[1]
            local limit = tonumber(ARGV[2])
            local userCouponId = ARGV[3]
            
            if redis.call('SISMEMBER', setKey, userId) == 1 then
                return 'DUPLICATE'
            end
            
            if redis.call('SCARD', setKey) >= limit then
                return 'LIMIT_EXCEEDED'
            end
            
            redis.call('SADD', setKey, userId)
            redis.call('RPUSH', queueKey, userCouponId)
            return 'OK'
        """;
        return RedisScript.of(script, String.class);
    }
}
