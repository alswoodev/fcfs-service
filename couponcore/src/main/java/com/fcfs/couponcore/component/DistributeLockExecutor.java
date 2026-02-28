package com.fcfs.couponcore.component;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class DistributeLockExecutor {
    private final RedissonClient redissonClient;
    public void execute(String lockKey, long waitMiliSecond, long leaseMiliSecond, Runnable task) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(waitMiliSecond, leaseMiliSecond, TimeUnit.MILLISECONDS);
            if (!isLocked) {
                throw new IllegalStateException("Failed to acquire lock");
            }
            task.run();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }
        }
    }
}
