package com.nsuslab.member.exception;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public MemberException(final String errorMessage, final String errorCode) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public MemberException(final String errorMessage, final String errorCode, final String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public MemberException(MemberExceptionStatus memberExceptionStatus) {
        super(memberExceptionStatus.getMessage());
        this.errorCode = memberExceptionStatus.getCode();
        this.errorMessage = memberExceptionStatus.getMessage();
    }

    public MemberException(MemberExceptionStatus memberExceptionStatus, final String detailMessage) {
        super(detailMessage);
        this.errorCode = memberExceptionStatus.getCode();
        this.errorMessage = memberExceptionStatus.getMessage();
    }
}
