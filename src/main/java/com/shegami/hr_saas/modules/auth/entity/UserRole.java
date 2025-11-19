package com.shegami.hr_saas.modules.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
    @Id
    private String roleId;

    @Column(nullable = false, unique = true)
    private String name;


    @PrePersist
    public void generateRoleId() {
        if (this.roleId == null) {
            this.roleId = "ROLE-" + UUID.randomUUID();
        }
    }
}
