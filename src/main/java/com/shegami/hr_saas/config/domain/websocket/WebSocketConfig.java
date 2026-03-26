package com.shegami.hr_saas.config.domain.websocket;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.shared.model.UserContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.user.DefaultUserDestinationResolver;
import org.springframework.messaging.simp.user.UserDestinationResolver;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtDecoder, userService))
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                StompCommand command = accessor.getCommand();
                log.info("WebSocketConfig: preSend - Command: {}", command);

                if (StompCommand.CONNECT.equals(command)) {
                    String authHeader = accessor.getFirstNativeHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7).trim();

                        try {
                            Jwt jwt = jwtDecoder.decode(token);
                            String userId = jwt.getSubject();

                            User userEntity = userService.findUserByUserId(userId)
                                    .orElseThrow(() -> new UserNotFoundException("User not found"));

                            String tenantId = jwt.getClaim("X-Tenant-ID");
                            List<String> roles = jwt.getClaim("roles");

                            Collection<GrantedAuthority> authorities = roles.stream()
                                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                                    .collect(Collectors.toList());

                            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

                            accessor.setUser(auth);

                            // CRITICAL: Store in session attributes EARLY
                            Map<String, Object> attrs = accessor.getSessionAttributes();
                            if (attrs != null) {
                                attrs.put("userId", userId);
                                attrs.put("tenantId", tenantId);
                            }

                            log.info("✅ CONNECT successful - User: {}", userId);

                        } catch (Exception e) {
                            log.error("JWT validation failed for WebSocket CONNECT", e);
                        }
                    }
                } else if (StompCommand.SUBSCRIBE.equals(command)) {
                    Principal principal = accessor.getUser();
                    String destination = accessor.getDestination();
                    String sessionId = accessor.getSessionId();

                    log.info("SUBSCRIBE → destination={}, user={}, sessionId={}",
                            destination,
                            principal != null ? principal.getName() : "NULL",
                            sessionId);

                    // Re-attach if still missing (fallback)
                    if (principal == null || "NULL".equals(principal.getName())) {
                        Map<String, Object> attrs = accessor.getSessionAttributes();
                        if (attrs != null && attrs.containsKey("userId")) {
                            String userId = (String) attrs.get("userId");
                            Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                            accessor.setUser(auth);
                            log.info("Re-attached user from session attributes: {}", userId);
                        }
                    }
                }

                return message;
            }

            @Override
            public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())
                        || StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Principal p = accessor.getUser();
                    if (p != null) {
                        log.info("postSend - User confirmed for {}: {}", accessor.getCommand(), p.getName());
                    }
                }
            }
        });
    }
}