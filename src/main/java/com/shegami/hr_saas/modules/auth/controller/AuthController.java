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

import static com.shegami.hr_saas.modules.auth.utils.AuthUtils.setAccessTokenCookie;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    public final AuthService authService;


    @PostMapping("login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto,HttpServletResponse response) {
        LoginResponseDto loginResponseDto = authService.login(loginDto);
        Cookie cookie = setAccessTokenCookie(loginResponseDto.accessToken());
        response.addCookie(cookie);
        return new ResponseEntity<>(loginResponseDto, HttpStatus.OK);
    }

    @GetMapping("logout")
    public ResponseEntity<Object> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {

        String email = authentication.getName();

        UserDto userInfo = authService.getCurrentUserInfo(email);

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("signup")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterDto registerDto, HttpServletResponse response){

        RegisterResponseDto register = authService.register(registerDto);

        Cookie cookie = setAccessTokenCookie(register.getToken());
        response.addCookie(cookie);

        return new ResponseEntity<>(register, HttpStatus.OK);
    }
    @PostMapping("refresh")
    public ResponseEntity<Object> refresh(RefreshDto refreshDto){
        return null;
    }


    @PostMapping("accept-invite")
    public ResponseEntity<Object> acceptInvite(InviteDto inviteDto){
        return null;
    }

}
