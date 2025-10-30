package com.shegami.hr_saas.modules.auth.controller;


import com.shegami.hr_saas.modules.auth.dto.*;
import com.shegami.hr_saas.modules.auth.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    public final AuthService authService;


    @PostMapping("login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto) {
        LoginResponseDto loginResponseDto = authService.login(loginDto);
        return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
    }

    @PostMapping("signup")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterDto registerDto){

        RegisterResponseDto register = authService.register(registerDto);

        return new ResponseEntity<>(register, HttpStatus.OK);
    }
    @PostMapping("refresh")
    public ResponseEntity<Object> refresh(RefreshDto refreshDto){
        return null;
    }

    @PostMapping("invite")
    public ResponseEntity<Object> invite(InviteDto inviteDto){
        return null;
    }

    @PostMapping("accept-invite")
    public ResponseEntity<Object> acceptInvite(InviteDto inviteDto){
        return null;
    }

}
