package com.shegami.hr_saas.config.domain.websocket;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.shared.model.UserContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7).trim();
                    Jwt jwt = jwtDecoder.decode(token);
                    String userId = jwt.getSubject();
                    String tenantId = jwt.getClaim("X-Tenant-ID");

                    attributes.put("userId", userId);
                    attributes.put("tenantId", tenantId);

                    log.info("HandshakeInterceptor: Stored userId {} in session attributes", userId);
                } catch (Exception e) {
                    log.warn("Handshake JWT validation failed", e);
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
    }
}
