package com.shegami.hr_saas.shared.model;

public record UserContext(
        String userId,
        String tenantId,
        String email,
        String jwtToken
) {}
