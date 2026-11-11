package com.deskforge.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class SessionCookies {

    private static final String PATH = "/api/auth";

    private final SessionProperties properties;

    public SessionCookies(SessionProperties properties) {
        this.properties = properties;
    }

    public String name() {
        return properties.cookieName();
    }

    public ResponseCookie issue(String refreshToken) {
        return base(refreshToken).maxAge(properties.refreshTokenValidity()).build();
    }

    public ResponseCookie clear() {
        return base("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String value) {
        return ResponseCookie.from(properties.cookieName(), value)
                .httpOnly(true)
                .secure(properties.cookieSecure())
                .sameSite("Strict")
                .path(PATH);
    }
}
