package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.dto.RegisterDto;
import com.shegami.hr_saas.modules.auth.entity.User;


public interface UserService {

    User findUserByEmail(String email);
    boolean createUser(RegisterDto registerDto);

    void updateUser(User appUser);

}
