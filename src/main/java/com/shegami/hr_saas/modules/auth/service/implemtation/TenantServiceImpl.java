package com.shegami.hr_saas.modules.auth.service.implemtation;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.enums.SubscriptionPlan;
import com.shegami.hr_saas.modules.auth.enums.TenantStatus;
import com.shegami.hr_saas.modules.auth.exception.TenantNotFoundException;
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
        return tenantRepository.findById(tenantId).orElseThrow(()->new TenantNotFoundException("Tenant Not Found with id: " + tenantId));
    }

    @Override
    public Tenant createTenant(TenantDto tenantDto) {
        Tenant tenant = new Tenant();
        tenant.setName(tenantDto.getName());
        tenant.setDomain(tenantDto.getDomain());
        tenant.setPlan(SubscriptionPlan.STARTER);
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenantRepository.save(tenant);
    }
}
