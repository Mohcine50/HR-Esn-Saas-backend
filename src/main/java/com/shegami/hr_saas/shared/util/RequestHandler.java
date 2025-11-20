package com.shegami.hr_saas.shared.util;

import com.nimbusds.jose.shaded.gson.JsonObject;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class RequestHandler {

    public static String resolveToken(HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals("access_token"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }


    public static void writeResponse(HttpServletResponse response, int status, JsonObject jsonObject) throws IOException, IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonObject.toString());
        response.getWriter().flush();
    }


}
