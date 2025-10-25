package com.shegami.hr_saas.modules.auth.entity;

import com.shegami.hr_saas.modules.auth.enums.SubscriptionPlan;
import com.shegami.hr_saas.modules.auth.enums.TenantStatus;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Tenant}
 */
@Value
public class TenantDto implements Serializable {
    String tenantId;
    String name;
    String domain;
    SubscriptionPlan plan;
    TenantStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}