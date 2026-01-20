package com.example.demo.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieUtil {

    @Value("${cookie.refresh-token.name}")
    private String cookieName;

    @Value("${cookie.refresh-token.path}")
    private String cookiePath;

    @Value("${cookie.refresh-token.max-age}")
    private int maxAge;

    @Value("${cookie.refresh-token.http-only}")
    private boolean httpOnly;

    @Value("${cookie.refresh-token.secure}")
    private boolean secure;

    public Cookie createRefreshTokenCookie(String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    public Cookie createClearRefreshTokenCookie() {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0);
        return cookie;
    }

    public String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
