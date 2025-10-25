package com.shegami.hr_saas.modules.auth.entity;

import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseTenantEntity {


    private String firstName;
    private String lastName;


    private String email;
    private String password;
    private String phoneNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<UserRole> roles;

    @Enumerated(EnumType.STRING)
    private UserStatus status;



    private LocalDateTime lastLoginAt;



    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;
    @PrePersist
    public void generateUserId() {
        if (this.userId == null) {
            this.userId = "USR-" + UUID.randomUUID();
        }
    }

}