package com.shegami.hr_saas.modules.auth.dto;

import com.shegami.hr_saas.modules.auth.entity.UserRole;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link UserRole}
 */
@Value
public class UserRoleDto implements Serializable {
    String name;
}