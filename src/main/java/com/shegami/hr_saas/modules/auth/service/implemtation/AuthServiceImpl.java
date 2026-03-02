package com.shegami.hr_saas.modules.auth.service.implemtation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.dto.*;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.exception.UserAlreadyExistException;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.service.AuthService;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.notifications.dto.EmailVerificationMessage;
import com.shegami.hr_saas.modules.notifications.enums.VerificationType;
import com.shegami.hr_saas.modules.notifications.rabbitmq.publisher.EventPublisher;
import com.shegami.hr_saas.shared.model.UserContext;
import com.shegami.hr_saas.shared.util.TokenGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@AllArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {


    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TenantService tenantService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    private final EventPublisher eventPublisher;

    @Override
    public LoginResponseDto login(LoginDto loginDto) {
        String jwtAccessToken = AuthUser(loginDto.getEmail(), loginDto.getPassword());
        return new LoginResponseDto(jwtAccessToken, "AUTH SUCCESSFULLY");
    }

    @Transactional
    @Override
    public RegisterResponseDto register(RegisterDto registerDto) {
        log.info("Register user with e-mail: {}", registerDto.getEmail());

        userService.findUserByEmail(registerDto.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistException("User already exists, please try another email.");
        });

        Tenant tenant = tenantService.createTenant(TenantDto.builder()
                .name(registerDto.getCompanyName())
                .domain(registerDto.getCompanyDomain()).build());

        var createdUser = userService.createUser(UserDto.builder()
                .email(registerDto.getEmail())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .firstName(registerDto.getFirstName())
                .lastName(registerDto.getLastName())
                .phoneNumber(registerDto.getPhone())
                .build(), tenant);



        String jwtAccessToken = AuthUser(registerDto.getEmail(), registerDto.getPassword());


        eventPublisher.publishVerificationEmail(EmailVerificationMessage.builder()
                        .recipientEmail(registerDto.getEmail())
                        .recipientFirstName(registerDto.getFirstName())
                        .verificationType(VerificationType.EMAIL_VERIFICATION)
                        .userId(createdUser.getUserId())
                        .verificationToken(TokenGenerator.generateToken())
                        .companyName(registerDto.getCompanyName())
                        .tenantId(createdUser.getTenant().getTenantId())
                .build());

        return new RegisterResponseDto(jwtAccessToken, "REGISTER SUCCESSFULLY");
    }


    private String AuthUser(String email, String password) {


        User user = userService.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException("User NOT FOUND WITH email: " + email));


        user.setLastLoginAt(LocalDateTime.now());
        userService.updateUser(user);


        List<UserRoles> roles = user.getRoles().stream().map(UserRole::getName).toList();


        Collection<GrantedAuthority> authorities = new ArrayList<>(roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList());

        Instant instant = Instant.now();

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);


        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder().subject(user.getUserId()).issuedAt(instant).expiresAt(instant.plus(60, ChronoUnit.MINUTES)).issuer("auth-service").claim("roles", roles).claim("X-Tenant-ID", user.getTenant().getTenantId()).build();


        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(jwtClaimsSet));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userDetails.getUsername(), password, authorities));

        UserContextHolder.setCurrentUserContext(new UserContext(user.getUserId(), user.getTenant().getTenantId(), user.getEmail()));

        return jwt.getTokenValue();
    }


    @Override
    public UserDto getCurrentUserInfo(String email) {
        User user = userService.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toDto(user);
    }
}
