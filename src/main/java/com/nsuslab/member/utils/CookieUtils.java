package com.nsuslab.member.utils;

import jakarta.servlet.http.Cookie;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtils {
    public Cookie createCookie(String cookieName, String cookieValue, long expiresInSeconds) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) expiresInSeconds);
        return cookie;
    }
}
