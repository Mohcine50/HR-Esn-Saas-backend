package com.shegami.hr_saas.config.domain.jpa;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.shared.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@RequiredArgsConstructor
public class JpaAuditorAware implements AuditorAware<User> {


    private final UserRepository userRepository;

    @Override
    public Optional<User> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        UserContext userContext = UserContextHolder.getCurrentUserContext();



        return Optional.of(userRepository.getReferenceById(userContext.userId()));
    }
}
