package com.shegami.hr_saas.modules.auth.service.implemtation;

import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.exception.UserRoleNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRoleRepository;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class UserRoleImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;

    @Override
    public UserRole getUserRoleByName(String name) {
        return userRoleRepository.findByName(name).orElseThrow(()-> new UserRoleNotFoundException("Role not found"));
    }

    @Override
    public void addUserRole(UserRole userRole) {
        userRoleRepository.save(userRole);
    }
}
