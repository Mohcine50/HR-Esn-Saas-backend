package com.shegami.hr_saas.config.filters;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.shared.model.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static com.shegami.hr_saas.shared.util.RequestHandler.resolveToken;
import static com.shegami.hr_saas.shared.util.RequestHandler.writeResponse;


@AllArgsConstructor
@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of( "/api/auth/verify", "/api/auth/login", "/api/auth/signup", "/v3/api-docs", "/swagger-ui/", "/api/invitations/accept", "/api/invitations/validate" );

    private final JwtDecoder jwtDecoder;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException
    {

        String jwtToken;
        boolean tokenExpired;
        Instant issuerAt, expiredAt;

        JsonObject jsonObject = new JsonObject();

        var requestedURI = request.getRequestURI();

        if (isPublicEndpoint(requestedURI)) {
            filterChain.doFilter(request, response);
            return;
        }


        jwtToken = resolveToken(request);


        if (jwtToken == null) {
            jsonObject.addProperty("Message", "NO TOKEN PROVIDED");
            writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, jsonObject);
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(jwtToken);
            var userId = jwt.getSubject();
            User user = userService.findUserByUserId(userId)
                    .orElse(null);

            if (user == null) {
                jsonObject.addProperty("Message", "NO USER WITH THIS USERNAME");
                writeResponse(response, HttpServletResponse.SC_UNAUTHORIZED, jsonObject);
                return;
            }


            issuerAt = jwt.getIssuedAt();
            expiredAt = jwt.getExpiresAt();

            assert issuerAt != null;
            assert expiredAt != null;
            tokenExpired = issuerAt.isBefore(expiredAt);

            if (!tokenExpired) {
                jsonObject.addProperty("Message", "Token expired");
                writeResponse(response, HttpServletResponse.SC_FORBIDDEN, jsonObject);
                return;
            }

            List<String> roles = jwt.getClaim("roles");
            String tenantId = jwt.getClaim("X-Tenant-ID");

            Collection<GrantedAuthority> authorities = new ArrayList<>(roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .toList());

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    authorities
            );



            UserContextHolder.setCurrentUserContext(new UserContext(userId, tenantId, user.getEmail()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException exception) {
            jsonObject.addProperty("Message", "INVALID TOKEN");
            writeResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, jsonObject);
        }finally {
            UserContextHolder.clearCurrentUserContext();
        }

    }

    private boolean isPublicEndpoint(String uri) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(uri::contains);
    }

}
