package com.nsuslab.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @InjectMocks
    private RedisService redisService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @Test
    @DisplayName("Refresh Token 저장 성공")
    void saveRefreshToken_success() {
        // given
        String username = "testuser";
        String refreshToken = "token123";
        long ttl = 10000L;
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        // when
        redisService.saveRefreshToken(username, refreshToken, ttl);

        // then
        then(valueOps).should().set(
                eq("refresh:testuser"),
                eq("token123"),
                eq(ttl),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("Refresh Token 조회 성공")
    void getRefreshToken_success() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("refresh:testuser")).willReturn("token123");

        // when
        String result = redisService.getRefreshToken("testuser");

        // then
        assertThat(result).isEqualTo("token123");
        then(valueOps).should().get("refresh:testuser");
    }

    @Test
    @DisplayName("Refresh Token 조회 - 값 없을 때 null 반환")
    void getRefreshToken_empty() {
        // given
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.get("refresh:unknown")).willReturn(null);

        // when
        String result = redisService.getRefreshToken("unknown");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Refresh Token 삭제 성공")
    void deleteRefreshToken_success() {
        // when
        redisService.deleteRefreshToken("testuser");

        // then
        then(redisTemplate).should().delete("refresh:testuser");
    }

    @Test
    @DisplayName("블랙리스트 등록 성공")
    void setBlackList_success() {
        // given
        String token = "access123";
        String value = "logout";
        long expire = 5000L;
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        // when
        redisService.setBlackList(token, value, expire);

        // then
        then(valueOps).should().set(
                eq("BlackList:access123"),
                eq(value),
                eq(Duration.ofMillis(expire))
        );
    }
}