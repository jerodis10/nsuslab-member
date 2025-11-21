package com.nsuslab.member.security.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiResponseType {

    SUCCESS(HttpStatus.OK.value(), "Success"),

    UNAUTHORIZED_RESPONSE(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"),
    FORBIDDEN_RESPONSE(HttpStatus.FORBIDDEN.value(), "Forbidden"),
    NOT_FOUND_RESPONSE(HttpStatus.NOT_FOUND.value(), "Not Found"),
    METHOD_NOT_ALLOWED_RESPONSE(HttpStatus.METHOD_NOT_ALLOWED.value(), "Method Not Allowed"),
    TO_MANY_REQUESTS_RESPONSE(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests");

    private final int code;
    private final String message;

}