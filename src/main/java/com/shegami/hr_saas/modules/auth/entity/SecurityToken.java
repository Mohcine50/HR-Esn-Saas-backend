package com.shegami.hr_saas.modules.auth.entity;


import com.shegami.hr_saas.modules.auth.enums.TokenType;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "security_tokens")
public class SecurityToken extends BaseTenantEntity {
    @Id
    String token;

    private TokenType tokenType;

    private LocalDateTime expiresAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
