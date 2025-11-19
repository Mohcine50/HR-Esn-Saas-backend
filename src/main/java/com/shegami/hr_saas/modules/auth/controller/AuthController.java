package com.shegami.hr_saas.modules.auth.controller;


import com.shegami.hr_saas.modules.auth.dto.*;
import com.shegami.hr_saas.modules.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    public final AuthService authService;


    @PostMapping("login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto,HttpServletResponse response) {
        LoginResponseDto loginResponseDto = authService.login(loginDto);
        Cookie cookie = new Cookie("access_token", loginResponseDto.accessToken());
        cookie.setDomain("localhost"); // or your specific domain
        cookie.setPath("/");
        cookie.setSecure(true); // Only send over HTTPS
        cookie.setHttpOnly(true); // Prevent client-side script access
        cookie.setMaxAge(3600); // Expires in 1 hour (3600 seconds)
        response.addCookie(cookie);
        return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {

        log.info("me");

        String email = authentication.getName();

        UserDto userInfo = authService.getCurrentUserInfo(email);

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("signup")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterDto registerDto, HttpServletResponse response){

        RegisterResponseDto register = authService.register(registerDto);
        Cookie cookie = new Cookie("token", register.getToken());
        cookie.setDomain("localhost"); // or your specific domain
        cookie.setPath("/");
        cookie.setSecure(true); // Only send over HTTPS
        cookie.setHttpOnly(true); // Prevent client-side script access
        cookie.setMaxAge(3600); // Expires in 1 hour (3600 seconds)
        response.addCookie(cookie);

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
