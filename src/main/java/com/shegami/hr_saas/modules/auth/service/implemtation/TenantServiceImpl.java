package com.shegami.hr_saas.modules.auth.service.implemtation;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.repository.TenantRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;

    @Override
    public Tenant getTenant(String tenantId) {
        return null;
    }

    @Override
    public Tenant createTenant(TenantDto tenantDto) {
        Tenant tenant = new Tenant();
        tenant.setName(tenantDto.getName());
        tenant.setDomain(tenantDto.getDomain());
        return tenantRepository.save(tenant);
    }
}
