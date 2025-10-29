package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.entity.UserRole;

public interface UserRoleService {
    UserRole getUserRoleByName(String name);
    void addUserRole(UserRole userRole);
}
