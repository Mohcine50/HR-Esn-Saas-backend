package com.shegami.hr_saas.modules.auth.entity;


import com.shegami.hr_saas.modules.auth.enums.SubscriptionPlan;
import com.shegami.hr_saas.modules.auth.enums.TenantStatus;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tenants")
@EntityListeners(AuditingEntityListener.class)
public class Tenant {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    private String name;

    private String domain;

    @OneToOne
    private UploadFile imageUrl;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    public void generateTenantId() {
        if (this.tenantId == null) {
            this.tenantId = "TNT-" + UUID.randomUUID();
        }
    }


}
