package com.shegami.hr_saas.config.domain.jpa;

import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@AllArgsConstructor
@EnableJpaAuditing
public class JpaConfig {

    private final UserRoleService userRoleService;

    //@Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {

            userRoleService.addUserRole(new UserRole(null, UserRoles.ADMIN.toString()));
            userRoleService.addUserRole(new UserRole(null, UserRoles.MANAGER.toString()));
            userRoleService.addUserRole(new UserRole(null, UserRoles.FINANCIAL.toString()));
            userRoleService.addUserRole(new UserRole(null, UserRoles.CONSULTANT.toString()));

        };
    }

}