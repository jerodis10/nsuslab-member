package com.nsuslab.member.security.handler;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String key, int limit, long durationSeconds) {
        String redisKey = "rate:" + key;
        Long currentCount = redisTemplate.opsForValue().increment(redisKey, 1);

        if (currentCount == null) {
            currentCount = 1L;
            redisTemplate.opsForValue().set(redisKey, "1", durationSeconds, TimeUnit.SECONDS);
        } else if (currentCount == 1) {
            redisTemplate.expire(redisKey, durationSeconds, TimeUnit.SECONDS);
        }

        return currentCount <= limit;
    }
}
