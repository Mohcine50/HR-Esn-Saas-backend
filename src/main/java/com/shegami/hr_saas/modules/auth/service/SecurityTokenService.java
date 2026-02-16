package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.entity.SecurityToken;

public interface SecurityTokenService {
    void deleteToken(String token);
    void createToken(SecurityToken securityToken);
}
