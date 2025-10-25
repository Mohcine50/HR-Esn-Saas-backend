package com.shegami.hr_saas.shared.util;

import com.nimbusds.jose.shaded.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;

public class RequestHandler {

    public static String resolveToken(HttpServletRequest request) {

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return token;
        }
        return null;
    }


    public static void writeResponse(HttpServletResponse response, int status, JsonObject jsonObject) throws IOException, IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonObject.toString());
        response.getWriter().flush();
    }


}
