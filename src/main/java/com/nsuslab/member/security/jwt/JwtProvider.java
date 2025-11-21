package com.nsuslab.member.security.jwt;

import com.nsuslab.member.security.sec.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final SecretKey secretKey;

    public static final long ACCESS_TOKEN_EXP = 1000L * 60 * 15; // 15분
    public static final long REFRESH_TOKEN_EXP = 1000L * 60 * 60 * 24 * 14; // 14일

    // HttpServletRequest에서 AccessToken 가져오기
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // 토큰 남은 유효시간(ms) 계산
    public long getRemainingMillis(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

   // 토큰에서 Claim 추출
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰에서 인증 subject 추출
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    // 토큰에서 인증 정보 추출
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰 발급
    public String generateJwtToken(Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put("roles", authentication.getAuthorities());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + ACCESS_TOKEN_EXP))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    // Access Token 생성
    public String createAccessToken(Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());
        claims.put("roles", authentication.getAuthorities());

        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_EXP);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + REFRESH_TOKEN_EXP);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰 검증
    public boolean isValidToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException expiredJwtException) {
            log.error("Expired JWT token");
            throw expiredJwtException;
        } catch (UnsupportedJwtException unsupportedJwtException) {
            log.error("Unsupported JWT token");
            throw unsupportedJwtException;
        } catch (JwtException jwtException) {
            log.error("Invalid JWT token");
            throw jwtException;
        } catch (IllegalArgumentException illegalArgumentException) {
            log.error("JWT claims string is empty.");
            throw illegalArgumentException;
        }
    }

}
