package com.shegami.hr_saas.modules.auth.service.implemtation;

import com.shegami.hr_saas.modules.auth.dto.*;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.service.AuthService;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TenantService tenantService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDto login(LoginDto loginDto) {

        userService.findUserByEmail(loginDto.getEmail());

        String jwtAccessToken = AuthUser(loginDto.getEmail(), loginDto.getPassword());

        return new LoginResponseDto(jwtAccessToken,"AUTH SUCCESSFULLY");


    }

    @Transactional
    @Override
    public RegisterResponseDto register(RegisterDto registerDto) {

        userService.findUserByEmail(registerDto.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistException("User already exists, please try another email.");
        });

        Tenant tenant = tenantService.createTenant(TenantDto.builder()
                .name(registerDto.getCompanyName())
                .domain(registerDto.getCompanyDomain())
                .build());



        userService.createUser(UserDto.builder()
                        .email(registerDto.getEmail())
                        .password(passwordEncoder.encode(registerDto.getPassword()))
                        .firstName(registerDto.getFirstName())
                        .lastName(registerDto.getLastName())
                        .phoneNumber(registerDto.getPhone())
                .build(), tenant);

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
