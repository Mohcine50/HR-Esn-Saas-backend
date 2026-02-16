package com.shegami.hr_saas.modules.auth.dto;

import com.shegami.hr_saas.modules.auth.enums.TokenType;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.shegami.hr_saas.modules.auth.entity.SecurityToken}
 */
@Value
@Builder
public class SecurityTokenDto implements Serializable {
    String token;
    TokenType tokenType;
    LocalDateTime expiresAt;
    String userId;
    String tenantId;
}