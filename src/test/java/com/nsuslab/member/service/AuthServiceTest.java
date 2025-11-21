package com.nsuslab.member.service;

import com.nsuslab.member.api.dto.response.TokenResponse;
import com.nsuslab.member.security.jwt.JwtProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.ValueOperations;

import static com.nsuslab.member.security.jwt.JwtProvider.REFRESH_TOKEN_EXP;
import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RedisService redisService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Claims claims;

    private Authentication authentication;

    @BeforeEach
    void setup() {
        authentication = new UsernamePasswordAuthenticationToken("testuser", "password123");
    }

    @Test
    @DisplayName("로그인 성공 - Access/Refresh 발급 및 Redis 저장")
    void login_success() {
        // given
        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtProvider.createAccessToken(authentication)).willReturn("access-token");
        given(jwtProvider.createRefreshToken("testuser")).willReturn("refresh-token");

        // when
        TokenResponse token = authService.login("testuser", "password123");

        // then
        assertThat(token.getAccessToken()).isEqualTo("access-token");
        assertThat(token.getRefreshToken()).isEqualTo("refresh-token");

        then(redisService).should().saveRefreshToken("testuser", "refresh-token", REFRESH_TOKEN_EXP);
    }

    @Test
    @DisplayName("Refresh Token Rotation 성공")
    void reissue_success() {
        // given ──────────────────────────────────────────
        Cookie cookie = new Cookie("refreshToken", "old-refresh");
        given(request.getCookies()).willReturn(new Cookie[]{cookie});

        given(jwtProvider.isValidToken("old-refresh")).willReturn(true);
        given(jwtProvider.parseClaims("old-refresh")).willReturn(claims);
        given(claims.getSubject()).willReturn("testuser");

        given(redisService.getRefreshToken("testuser")).willReturn("old-refresh");

        // 새 토큰 발급
        given(jwtProvider.createRefreshToken("testuser")).willReturn("new-refresh");
        given(jwtProvider.createAccessToken(any())).willReturn("new-access");

        // when ──────────────────────────────────────────
        authService.reissue(request, response);

        // then ──────────────────────────────────────────
        then(redisService).should().deleteRefreshToken("testuser");
        then(redisService).should().saveRefreshToken("testuser", "new-refresh", 10000000L);
        then(response).should().addCookie(any());
    }


    @Test
    @DisplayName("재발급 실패 - 쿠키 없음")
    void reissue_fail_no_cookie() {
        // given
        given(request.getCookies()).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token missing");
    }


    @Test
    @DisplayName("재발급 실패 - Refresh Token 유효하지 않음")
    void reissue_fail_invalid_token() {
        Cookie cookie = new Cookie("refreshToken", "invalid");
        given(request.getCookies()).willReturn(new Cookie[]{cookie});
        given(jwtProvider.isValidToken("invalid")).willReturn(false);

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
    }


    @Test
    @DisplayName("재발급 실패 - Redis에 저장된 토큰 없음(로그아웃된 토큰)")
    void reissue_fail_no_saved_token() {
        Cookie cookie = new Cookie("refreshToken", "old-refresh");
        given(request.getCookies()).willReturn(new Cookie[]{cookie});
        given(jwtProvider.isValidToken("old-refresh")).willReturn(true);
        given(jwtProvider.parseClaims("old-refresh")).willReturn(claims);
        given(claims.getSubject()).willReturn("testuser");

        given(redisService.getRefreshToken("testuser")).willReturn(null);

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("로그아웃된 토큰");
    }


    @Test
    @DisplayName("재발급 실패 - 사용된 Refresh Token (Rotation Attack)")
    void reissue_fail_token_mismatch() {
        Cookie cookie = new Cookie("refreshToken", "client-token");
        given(request.getCookies()).willReturn(new Cookie[]{cookie});

        given(jwtProvider.isValidToken("client-token")).willReturn(true);
        given(jwtProvider.parseClaims("client-token")).willReturn(claims);
        given(claims.getSubject()).willReturn("testuser");

        given(redisService.getRefreshToken("testuser")).willReturn("stored-token");

        assertThatThrownBy(() -> authService.reissue(request, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh Token이 이미 사용됨");
    }

    @Test
    @DisplayName("로그아웃 성공 - Refresh 삭제 + Access 블랙리스트 등록")
    void logout_success() {
        // given
        Cookie cookie = new Cookie("refreshToken", "refresh123");
        given(request.getCookies()).willReturn(new Cookie[]{cookie});

        given(jwtProvider.parseClaims("refresh123")).willReturn(claims);
        given(claims.getSubject()).willReturn("testuser");
        given(claims.getExpiration()).willReturn(
                new Date(System.currentTimeMillis() + 60000) // 1분 후 만료
        );

        // RedisTemplate mocking
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(ops);

        // when
        authService.logout(request, response);

        // then
        then(redisService).should().deleteRefreshToken("testuser");
        then(ops).should().set(
                eq("blacklist:refresh123"),
                eq("logout"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }
}