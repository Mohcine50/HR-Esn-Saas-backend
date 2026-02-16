package com.shegami.hr_saas.modules.auth.service.implemtation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.dto.SecurityTokenDto;
import com.shegami.hr_saas.modules.auth.entity.SecurityToken;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.mapper.SecurityTokenMapper;
import com.shegami.hr_saas.modules.auth.repository.SecurityTokenRepository;
import com.shegami.hr_saas.modules.auth.repository.TenantRepository;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.SecurityTokenService;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityTokenServiceImpl implements SecurityTokenService {

    private final SecurityTokenRepository securityTokenRepository;
    private final UserService userService;
    private final TenantService tenantService;
    private final SecurityTokenMapper securityTokenMapper;

    @Override
    public void deleteToken(String token) {
        log.info("delete token: {}", token);
        securityTokenRepository.deleteByToken(token);
    }

    @Override
    public void createToken(SecurityTokenDto securityTokenDto) {
        log.info("createToken: {}", securityTokenDto.getToken());

        Tenant tenant = tenantService.getTenant(securityTokenDto.getTenantId());

        User user = userService.findUserByUserId(securityTokenDto.getUserId()).orElseThrow(()->new UserNotFoundException("User not found"));

        SecurityToken securityToken = securityTokenMapper.toEntity(securityTokenDto);

        securityToken.setUser(user);
        securityToken.setTenant(tenant);

        securityTokenRepository.save(securityToken);
    }
}
