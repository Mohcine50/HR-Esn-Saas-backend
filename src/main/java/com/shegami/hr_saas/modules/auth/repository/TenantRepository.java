package com.shegami.hr_saas.modules.auth.repository;

import com.shegami.hr_saas.modules.auth.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, String> {
}