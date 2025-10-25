package com.shegami.hr_saas.modules.auth.dto;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link User}
 */
@Value
@Builder
public class UserDto implements Serializable {
    @NotNull
    TenantDto tenant;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String firstName;
    String lastName;
    String email;
    String password;
    String phoneNumber;
    List<UserRoleDto> roles;
    UserStatus status;
    LocalDateTime lastLoginAt;
    String userId;
}