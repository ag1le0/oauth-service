package com.foxconn.fii.common.utils;

import lombok.experimental.UtilityClass;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@UtilityClass
public class CookieUtils {

    public Cookie getCookie(HttpServletRequest request, String name) {
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
}
