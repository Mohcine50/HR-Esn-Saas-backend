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

  @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenantId AND m.consultant.consultantId = :consultantId")
  Page<Mission> findByConsultantIdAndTenantId(Pageable pageable, @Param("tenantId") String tenantId, @Param("consultantId") String consultantId);

  @Query("SELECT COUNT(m) > 0 FROM Mission m WHERE m.consultant.consultantId = :consultantId " +
          "AND m.status = 'ACTIVE' " +
          "AND (m.startDate <= :endDate AND m.endDate >= :startDate)")
  boolean existsByConsultantIdAndDateRange(String consultantId, LocalDate startDate, LocalDate endDate);

  @Query("SELECT COUNT(m) > 0 FROM Mission m WHERE m.consultant.consultantId = :consultantId " +
          "AND m.mission_id <> :currentMissionId " +
          "AND m.status = 'ACTIVE' " +
          "AND (m.startDate <= :endDate AND m.endDate >= :startDate)")
  boolean existsOverlapForUpdate(String consultantId, LocalDate startDate, LocalDate endDate, String currentMissionId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("UPDATE Mission SET Consultant = :consultant WHERE Mission .mission_id = :missionId")
  int assignConsultantToMission(@Param("missionId") String missionId,@Param("consultant") Consultant consultant);
}