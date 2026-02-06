package com.shegami.hr_saas.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_settings")
public class UserSettings {


    private boolean emailNotificationsEnabled = true;
    private boolean pushNotificationsEnabled = true;

    private String language = "en";

    private String timezone = "UTC";
    private String locale = "en-US";
    private String twoFactorAuth = "false";

    @Id
    @Column(name = "user_settings_id", nullable = false)
    private String id;
    @PrePersist
    public void generateUserId() {
        if (this.id == null) {
            this.id = "SET-" + UUID.randomUUID();
        }
    }

}