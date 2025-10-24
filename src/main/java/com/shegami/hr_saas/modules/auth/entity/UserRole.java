package com.shegami.hr_saas.modules.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class UserRole {
    @Id
    private String roleId;

    private String name;


    @PrePersist
    public void generateRoleId() {
        if (this.roleId == null) {
            this.roleId = "ROLE-" + UUID.randomUUID();
        }
    }
}
