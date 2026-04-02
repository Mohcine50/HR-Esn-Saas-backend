package com.shegami.hr_saas.modules.auth.controller;

import com.shegami.hr_saas.modules.auth.dto.*;
import com.shegami.hr_saas.modules.auth.service.AuthService;
import com.shegami.hr_saas.modules.auth.service.SecurityTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.shegami.hr_saas.config.domain.context.UserContextHolder;

import java.util.Map;

import static com.shegami.hr_saas.modules.auth.utils.AuthUtils.setAccessTokenCookie;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final SecurityTokenService securityTokenService;

    @PostMapping("login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
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
    public ResponseEntity<Object> me() {
        String userId = UserContextHolder.getCurrentUserContext().userId();
        String accessToken = UserContextHolder.getCurrentUserContext().jwtToken();
        UserDto userInfo = authService.getCurrentUserInfo(userId);
        return ResponseEntity.ok(Map.of("user", userInfo, "accessToken", accessToken));
    }

    @PostMapping("signup")
    public ResponseEntity<RegisterResponseDto> register(@RequestBody RegisterDto registerDto,
            HttpServletResponse response) {

        RegisterResponseDto register = authService.register(registerDto);

        Cookie cookie = setAccessTokenCookie(register.getToken());
        response.addCookie(cookie);

        return new ResponseEntity<>(register, HttpStatus.OK);
    }

    @PostMapping("refresh")
    public ResponseEntity<Object> refresh(RefreshDto refreshDto) {
        return null;
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyAccount(@RequestParam String token) {
        boolean valid = securityTokenService.verifyAccount(token);
        return ResponseEntity.ok(Map.of(
                "verified", valid,
                "message", valid ? "VERIFIED" : "INVALID_OR_EXPIRED"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        authService.changePassword(changePasswordDto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateUserInfo(@Valid @RequestBody UpdateUserInfoDto updateUserInfoDto) {
        authService.updateUserInfo(updateUserInfoDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification() {
        authService.resendVerificationEmail();
        return ResponseEntity.ok().build();
    }

}
