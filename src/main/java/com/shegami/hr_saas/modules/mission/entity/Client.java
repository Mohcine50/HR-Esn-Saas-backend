package com.shegami.hr_saas.modules.mission.entity;


import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "clients")
public class Client extends BaseTenantEntity {


    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    //TODO: NEED TO CREATE SEPARATE ENTITY FOR ADDRESSES
    private String address;

    @Column(name = "vat_number")
    private String vatNumber;

    @OneToMany(fetch = FetchType.LAZY)
    private Set<Project> projects;

    @Id
    @Column(name = "client_id", nullable = false)
    private String clientId;
    @PrePersist
    public void generateClientId() {
        if (this.clientId == null) {
            this.clientId = "CLT-" + UUID.randomUUID();
        }
    }

}
