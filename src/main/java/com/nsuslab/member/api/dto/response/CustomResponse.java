package com.nsuslab.member.api.dto.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomResponse<T> {
    private String code;
    private String message;

    public CustomResponse(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public static <Void> CustomResponse<Void> empty() {
        return new CustomResponse<>(null, null);
    }

    public static <Void> CustomResponse<Void> error(final String code) {
        return new CustomResponse<>(code, null);
    }

    public static <Void> CustomResponse<Void> error(final String code, final String message) {
        return new CustomResponse<>(code, message);
    }

    public static <T> CustomResponse<T> of(final String code) {
        return new CustomResponse<>(code, null);
    }

    public static <T> CustomResponse<T> of(final String code, final String message) {
        return new CustomResponse<>(code, message);
    }
}
