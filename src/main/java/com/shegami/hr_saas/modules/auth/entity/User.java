package com.shegami.hr_saas.modules.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.entity.Invitation;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
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

    @JsonIgnore
    private String password;

    private String phoneNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<UserRole> roles = new ArrayList<>();

    @OneToOne
    private UploadFile imageUrl;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime lastLoginAt;

    private Boolean isEmailVerified = false;
    private LocalDateTime emailVerifiedAt;

    private boolean isPending;

    @OneToOne
    @JoinColumn(name = "created_from_invitation_id")
    private Invitation createdFromInvitation;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "user")
    private Employee employee;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "settings_id")
    private UserSettings userSettings;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @PrePersist
    public void generateUserId() {
        if (this.userId == null) {
            this.userId = "USR-" + UUID.randomUUID();
        }
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

}