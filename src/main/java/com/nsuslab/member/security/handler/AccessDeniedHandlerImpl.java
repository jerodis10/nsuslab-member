package com.nsuslab.member.security.handler;

import com.nsuslab.member.security.common.ApiResponse;
import com.nsuslab.member.security.common.ApiResponseType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        ApiResponse.error(response, ApiResponseType.FORBIDDEN_RESPONSE);
    }

}
