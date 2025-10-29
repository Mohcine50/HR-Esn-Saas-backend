package com.shegami.hr_saas.shared.exception;

import com.nimbusds.jose.shaded.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.io.IOException;

@ControllerAdvice
public class CustomAccessDeniedHandler implements AccessDeniedHandler {



    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("Message","YOU DON'T HAVE ENOUGH PERMISSION" );
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonObject.toString());
    }
}