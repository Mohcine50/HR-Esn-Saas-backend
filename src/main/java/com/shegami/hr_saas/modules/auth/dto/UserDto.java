package com.shegami.hr_saas.modules.auth.dto;

import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link com.shegami.hr_saas.modules.auth.entity.User}
 */
@Value
@Builder
public class UserDto implements Serializable {
    TenantDto tenant;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String firstName;
    String lastName;
    String email;
    String phoneNumber;
    List<UserRoleDto> roles;
    String profileUrl;
    UserStatus status;
    LocalDateTime lastLoginAt;
    Boolean isEmailVerified;
    LocalDateTime emailVerifiedAt;
    boolean isPending;
    UserSettingsDto userSettings;
    String userId;
    String password;
}