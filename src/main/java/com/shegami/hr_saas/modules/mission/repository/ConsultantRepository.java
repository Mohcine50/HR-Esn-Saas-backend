package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConsultantRepository extends JpaRepository<Consultant, String> {
    @Query("SELECT c FROM Consultant c WHERE c.tenant.tenantId = :tenant")
    Page<Consultant> findByTenantId(Pageable pageable, @Param("tenant") String tenant);
    Optional<Consultant> findByEmail(String email);
    boolean existsByEmail(String email);
}