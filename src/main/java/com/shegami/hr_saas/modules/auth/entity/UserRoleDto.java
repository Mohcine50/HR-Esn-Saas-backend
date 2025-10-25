package com.shegami.hr_saas.modules.auth.entity;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link UserRole}
 */
@Value
public class UserRoleDto implements Serializable {
    String name;
}