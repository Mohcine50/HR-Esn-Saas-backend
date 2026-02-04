package com.shegami.hr_saas.modules.mission.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, String> {
  @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenant")
  Page<Mission> findByTenantId(Pageable pageable, @Param("tenant") String tenant);

  Optional<Mission> findByConsultant_ConsultantId(String consultantId);
}