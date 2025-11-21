package com.nsuslab.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_PREFIX = "refresh:";

    public void saveRefreshToken(String username, String refreshToken, long ttlMillis) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(REFRESH_PREFIX + username, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(String username) {
        return redisTemplate.opsForValue().get(REFRESH_PREFIX + username);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(REFRESH_PREFIX + username);
    }

    public void setBlackList(String token, String value, long expireMillis) {
        redisTemplate.opsForValue().set("BlackList:" + token, value, Duration.ofMillis(expireMillis));
    }
}
