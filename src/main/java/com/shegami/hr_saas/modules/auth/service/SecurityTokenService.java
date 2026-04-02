package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.dto.SecurityTokenDto;
import com.shegami.hr_saas.modules.auth.entity.SecurityToken;

public interface SecurityTokenService {
    void deleteToken(String token);

    void createToken(SecurityTokenDto securityToken);

    boolean verifyAccount(String token);

    void deleteTokensByUser(String userId);
}
