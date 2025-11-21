package com.nsuslab.member.security.sec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nsuslab.member.api.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
        setFilterProcessesUrl("/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 사용자 요청 정보로 UserPasswordAuthenticationToken 발급
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            // UsernamePasswordAuthenticationToken 생성
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword());

            // 추가 세부 정보 설정 (IP, 세션 등)
            setDetails(request, authRequest);

            // AuthenticationManager에 전달 -> AuthenticationProvider에서 검증
            return this.getAuthenticationManager().authenticate(authRequest);

//        final UsernamePasswordAuthenticationToken authRequest;
//        final Member member;
//        try {
//            // 사용자 요청 정보로 UserPasswordAuthenticationToken 발급
////            String refreshToken = Arrays.stream(request.getCookies() == null ? new Cookie[0] : request.getCookies())
////                    .filter(c -> "refreshToken".equals(c.getName()))
////                    .map(Cookie::getValue)
////                    .findFirst()
////                    .orElse(null);
////
////            String loginId = jwtProvider.getSubject(refreshToken);
////            String password = jwtProvider.getSubject(refreshToken);
//
////            LoginRequest loginRequest = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);
//            member = new ObjectMapper().readValue(request.getInputStream(), Member.class);
//
//            authRequest = new UsernamePasswordAuthenticationToken(member.getLoginId(), member.getPassword());
////            authRequest = new UsernamePasswordAuthenticationToken(loginId, "1111");
        } catch (IOException e) {
            throw new RuntimeException("Token 발급 실패");
        }

//        setDetails(request, authRequest);

        // AuthenticationManager에게 전달 -> AuthenticationProvider의 인증 메서드 실행
//        return this.getAuthenticationManager().authenticate(authRequest);
    }

}
