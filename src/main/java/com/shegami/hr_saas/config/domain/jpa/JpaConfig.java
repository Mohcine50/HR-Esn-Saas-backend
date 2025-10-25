package com.shegami.hr_saas.config.domain.jpa;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@AllArgsConstructor
public class JpaConfig {

    private final AuditorAware<?> auditorAware;

    @Bean
    public AuditorAware<?> auditorAware() {
        return auditorAware;
    }
}