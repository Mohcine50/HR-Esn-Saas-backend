package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;

public interface UserRoleService {
    UserRole getUserRoleByName(UserRoles name);
    void addUserRole(UserRole userRole);
}
