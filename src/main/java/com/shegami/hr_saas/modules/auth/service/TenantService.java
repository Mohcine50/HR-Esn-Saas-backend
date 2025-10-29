package com.shegami.hr_saas.modules.auth.service;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.entity.Tenant;

public interface TenantService {
    Tenant getTenant(String tenantId);
    Tenant createTenant(TenantDto tenantDto);
}
