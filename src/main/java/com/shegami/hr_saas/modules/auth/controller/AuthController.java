package com.shegami.hr_saas.modules.auth.controller;


import com.shegami.hr_saas.modules.auth.dto.InviteDto;
import com.shegami.hr_saas.modules.auth.dto.LoginDto;
import com.shegami.hr_saas.modules.auth.dto.RefreshDto;
import com.shegami.hr_saas.modules.auth.dto.RegisterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/auth")
public class AuthController {


    @PostMapping("login")
    public ResponseEntity<Object> login(LoginDto loginDto) {
        return null;
    }

    @PostMapping("signup")
    public ResponseEntity<Object> register(RegisterDto registerDto){
        return null;
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
