package com.shegami.hr_saas.modules.auth.entity;


import com.shegami.hr_saas.modules.auth.enums.TenantStatus;
import com.shegami.hr_saas.modules.auth.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    private String name;

    private String domain;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateTenantId() {
        if (this.tenantId == null) {
            this.tenantId = "TNT-" + UUID.randomUUID();
        }
    }


}
