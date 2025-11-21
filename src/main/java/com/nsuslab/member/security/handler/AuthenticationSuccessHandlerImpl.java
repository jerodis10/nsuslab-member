package com.nsuslab.member.security.handler;

import com.nsuslab.member.security.common.ApiResponseType;
import com.nsuslab.member.security.jwt.JwtProvider;
import com.nsuslab.member.service.RedisService;
import com.nsuslab.member.utils.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import static com.nsuslab.member.security.jwt.JwtProvider.REFRESH_TOKEN_EXP;

@Component
@RequiredArgsConstructor
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 전달받은 인증정보 SecurityContextHolder에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // JWT Token 발급
        String username = authentication.getName();
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(username);

        // Redis에 Refresh Token 저장
        redisService.saveRefreshToken(username, refreshToken, REFRESH_TOKEN_EXP);
        responseToken(response, refreshToken, accessToken);
    }

    private void responseToken(HttpServletResponse response, String refreshToken, String accessToken) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(ApiResponseType.SUCCESS.getCode());

        Cookie refreshCookie = CookieUtils.createCookie("refreshToken", refreshToken, REFRESH_TOKEN_EXP);
        response.addCookie(refreshCookie);
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

}
