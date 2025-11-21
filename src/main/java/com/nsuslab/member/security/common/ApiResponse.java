package com.nsuslab.member.security.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Objects;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiResponse {

    private int code = ApiResponseType.SUCCESS.getCode();
    private String msg = ApiResponseType.SUCCESS.getMessage();

    public static ApiResponse error(ApiResponseType apiResponseType) {
        return new ApiResponse(apiResponseType.getCode(), apiResponseType.getMessage());
    }

    public static ApiResponse error(ApiResponseType apiResponseType, String Message) {
        return new ApiResponse(apiResponseType.getCode(), Message);
    }

    public static void error(ServletResponse response, ApiResponseType apiResponseType) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setStatus(apiResponseType.getCode());
            httpServletResponse.getWriter().write(Objects.requireNonNull(objectMapper.writeValueAsString(ApiResponse.error(apiResponseType))));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void token(ServletResponse response, String token) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            httpServletResponse.setCharacterEncoding("UTF-8");
            httpServletResponse.setStatus(ApiResponseType.SUCCESS.getCode());

            Cookie cookie = new Cookie("Authorization", token);
            cookie.setPath("/");
            httpServletResponse.addCookie(cookie);

            httpServletResponse.getWriter().write(Objects.requireNonNull(objectMapper.writeValueAsString(new ApiResponse())));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
