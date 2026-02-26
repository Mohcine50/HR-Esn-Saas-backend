package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Consultant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, String> {
  @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenantId")
  Page<Mission> findByTenantId(Pageable pageable, @Param("tenantId") String tenantId);

  @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenantId AND (:consultant) MEMBER OF m.consultants")
  Page<Mission> findByConsultantIdAndTenantId(Pageable pageable, @Param("tenantId") String tenantId, @Param("consultant") Consultant consultant);

}