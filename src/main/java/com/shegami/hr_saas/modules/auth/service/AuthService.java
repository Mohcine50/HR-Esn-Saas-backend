package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.dto.*;

public interface AuthService {

    LoginResponseDto login(LoginDto loginDto);

    RegisterResponseDto register(RegisterDto registerDto);

    UserDto getCurrentUserInfo(String userId);

}
