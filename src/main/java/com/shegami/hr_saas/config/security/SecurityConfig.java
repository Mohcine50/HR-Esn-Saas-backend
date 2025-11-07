package com.shegami.hr_saas.config.security;


import com.shegami.hr_saas.config.filters.JwtAuthFilter;
import com.shegami.hr_saas.shared.exception.CustomAccessDeniedHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.regex.Pattern;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;


@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        RequestMatcher shortUrlMatcher = request -> {
            String path = request.getRequestURI();
            return request.getMethod().equals("GET")
                    && Pattern.matches("^/[A-Za-z0-9]{8}$", path);
        };

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(shortUrlMatcher).permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/signup").permitAll()
                        .requestMatchers(antMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers(antMatcher("/swagger-ui/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exception -> exception.accessDeniedHandler(customAccessDeniedHandler))
                .cors(Customizer.withDefaults())
                .build();
    }

}
