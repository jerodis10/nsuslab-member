package com.nsuslab.member.security.handler;

import com.nsuslab.member.security.jwt.JwtProvider;
import com.nsuslab.member.service.RedisService;
import com.nsuslab.member.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandlerImpl implements LogoutHandler {
    private final RedisService redisService;
    private final JwtProvider jwtProvider;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        // 1. 헤더에서 Access Token 가져오기
        String accessToken = jwtProvider.resolveAccessToken(request);

        if (accessToken != null && jwtProvider.isValidToken(accessToken)) {

            // 2. Access Token 유효하면 username 추출
            String username = jwtProvider.getUsername(accessToken);

            // 3. Redis에서 Refresh Token 삭제
            redisService.deleteRefreshToken(username);

            // 4. Access Token 블랙리스트 등록 (만료 전까지)
            long remainingMillis = jwtProvider.getRemainingMillis(accessToken);
            redisService.setBlackList(accessToken, "logout", remainingMillis);
        }

        // 5. 쿠키 삭제 (Refresh Token)
        Cookie cookie = CookieUtils.createCookie("refreshToken", null, 0);
        response.addCookie(cookie);
    }
}
