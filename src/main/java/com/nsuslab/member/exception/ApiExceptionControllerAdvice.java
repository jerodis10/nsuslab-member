package com.nsuslab.member.exception;

import com.nsuslab.member.api.dto.response.CustomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ApiExceptionControllerAdvice extends ResponseEntityExceptionHandler {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MemberException.class)
    public CustomResponse<Void> handleStationException(MemberException e) {
        log.error("handleStationException : {}", e.getMessage());

        return CustomResponse.error(e.getErrorCode(), e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public CustomResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("handleIllegalArgumentException : {}", e.getMessage());

        return CustomResponse.error(MemberExceptionStatus.WRONG_ARGUMENT.getCode(), MemberExceptionStatus.WRONG_ARGUMENT.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(Exception.class)
    public CustomResponse<Void> handleAllException(Exception e) {
        log.warn("handleAllException : {}", e.getMessage());

        return CustomResponse.error(MemberExceptionStatus.INTERNAL_SERVER_ERROR.getCode(), MemberExceptionStatus.INTERNAL_SERVER_ERROR.getMessage());
    }

}
