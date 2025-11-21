package com.nsuslab.member.service;

import com.nsuslab.member.api.dto.response.TokenResponse;
import com.nsuslab.member.security.jwt.JwtProvider;
import com.nsuslab.member.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.nsuslab.member.security.jwt.JwtProvider.REFRESH_TOKEN_EXP;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final AuthenticationManager authenticationManager;
    private final StringRedisTemplate redisTemplate;

    // 로그인
    public TokenResponse login(String username, String password) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(username);

        // Redis에 Refresh Token 저장
        redisService.saveRefreshToken(username, refreshToken, REFRESH_TOKEN_EXP);

        return new TokenResponse(accessToken, refreshToken);
    }

    // Refresh Token Rotation
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        // 1. get cookie
        String refreshToken = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Refresh token missing"));

        // 2. validate token
        if (!jwtProvider.isValidToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);
        String username = claims.getSubject();

        // Redis에 저장된 기존 Refresh Token 불러오기
        String savedToken = redisService.getRefreshToken(username);
        if (savedToken == null) {
            throw new RuntimeException("로그아웃된 토큰");
        }

        if (!savedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh Token이 이미 사용됨 (Rotation)");
        }

        // 기존 Refresh Token 폐기
        redisService.deleteRefreshToken(username);

        // 새 Refresh Token 발급
        String newRefreshToken = jwtProvider.createRefreshToken(username);

        // 새 Refresh Token 저장
        redisService.saveRefreshToken(username, newRefreshToken, 10000000L);

        // Access Token도 새로 발급
        Authentication auth = new UsernamePasswordAuthenticationToken(username, null, null);
        String newAccessToken = jwtProvider.createAccessToken(auth);

        Cookie cookie = CookieUtils.createCookie("refreshToken", newRefreshToken, REFRESH_TOKEN_EXP);
//        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        cookie.setMaxAge((int) Duration.ofDays(7).getSeconds());
        response.addCookie(cookie);
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);

        new TokenResponse(newAccessToken, newRefreshToken);
    }

    // 로그아웃
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
                .filter(c -> "refreshToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        Claims claims = jwtProvider.parseClaims(refreshToken);
        String username = claims.getSubject();

        redisService.deleteRefreshToken(username);

        // Access Token 블랙리스트 추가(추가하면 더 안전)
        long expire = claims.getExpiration().getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set("blacklist:" + refreshToken, "logout", expire, TimeUnit.MILLISECONDS);
    }
}
