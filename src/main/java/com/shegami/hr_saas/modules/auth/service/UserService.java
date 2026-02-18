package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;

import java.util.Optional;


public interface UserService {

    Optional<User> findUserByEmail(String email);
    Optional<User> findUserByUserId(String userId);
    User createUser(UserDto userDto, Tenant tenant);
    void updateUser(User appUser);

}
