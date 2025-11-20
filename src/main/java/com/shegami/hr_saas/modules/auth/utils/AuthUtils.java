package com.shegami.hr_saas.modules.auth.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthUtils {

    public static Cookie setAccessTokenCookie(String accessToken) {

        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setDomain("localhost"); // or your specific domain
        cookie.setPath("/");
        cookie.setSecure(true); // Only send over HTTPS
        cookie.setHttpOnly(true); // Prevent client-side script access
        cookie.setMaxAge(3600); // Expires in 1 hour (3600 seconds)
        return cookie;
    }

}
