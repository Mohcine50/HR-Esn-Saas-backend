package com.shegami.hr_saas.modules.auth.service;


import com.shegami.hr_saas.modules.auth.dto.LoginDto;
import com.shegami.hr_saas.modules.auth.dto.LoginResponseDto;
import com.shegami.hr_saas.modules.auth.dto.RegisterDto;
import com.shegami.hr_saas.modules.auth.dto.RegisterResponseDto;


public interface AuthService {

LoginResponseDto login(LoginDto loginDto);
RegisterResponseDto register(RegisterDto registerDto);

}
