package com.shegami.hr_saas.modules.auth.dto;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.shegami.hr_saas.modules.auth.entity.UserSettings}
 */
@Value
public class UserSettingsDto implements Serializable {
    boolean emailNotificationsEnabled;
    boolean pushNotificationsEnabled;
    String language;
    String timezone;
    String locale;
    String twoFactorAuth;
    String id;

}