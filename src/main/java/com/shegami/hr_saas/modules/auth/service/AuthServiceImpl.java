package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.dto.LoginDto;
import com.shegami.hr_saas.modules.auth.dto.LoginResponseDto;
import com.shegami.hr_saas.modules.auth.dto.RegisterDto;
import com.shegami.hr_saas.modules.auth.dto.RegisterResponseDto;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.shared.exception.ApiRequestException;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Override
    public LoginResponseDto login(LoginDto loginDto) {

        userService.findUserByEmail(loginDto.getEmail());

        String jwtAccessToken = AuthUser(loginDto.getEmail(), loginDto.getPassword());

        return new LoginResponseDto(jwtAccessToken,"AUTH SUCCESSFULLY");


    }

    @Override
    public RegisterResponseDto register(RegisterDto registerDto) {

        User user = userService.findUserByEmail(registerDto.getEmail());

        if (user != null) {
            throw new ApiRequestException("Username already exists");
        }


        boolean userRegistered = userService.createUser(registerDto);

        if (!userRegistered) {
            throw new ApiRequestException("REGISTER NOT COMPLETED");
        }

        String jwtAccessToken = AuthUser(registerDto.getEmail(), registerDto.getPassword());
        return new RegisterResponseDto(jwtAccessToken,"REGISTER SUCCESSFULLY");
    }


    private String AuthUser(String email, String password) {



        Instant instant = Instant.now();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        String scope = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(" "));

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(instant)
                .expiresAt(instant.plus(60, ChronoUnit.MINUTES))
                .issuer("auth-service")
                .claim("scope", scope)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet)).getTokenValue();

    }
}
