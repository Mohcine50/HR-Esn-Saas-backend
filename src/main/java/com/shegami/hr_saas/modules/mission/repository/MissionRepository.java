package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Consultant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.repository.Searchable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, String>, Searchable {
  @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenantId")
  Page<Mission> findByTenantId(Pageable pageable, @Param("tenantId") String tenantId);

  @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenantId AND (:consultant) MEMBER OF m.consultants")
  Page<Mission> findByConsultantIdAndTenantId(Pageable pageable, @Param("tenantId") String tenantId,
      @Param("consultant") Consultant consultant);

  Optional<Mission> findByMissionIdAndTenantTenantId(String missionId, String tenantId);

  @Query(value = """
          SELECT m.mission_id AS id,
                 m.title AS name
          FROM missions m
          WHERE m.tenant_id = :tenantId
            AND (
              :search IS NULL
              OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))
            )
          ORDER BY m.title ASC
      """, countQuery = """
          SELECT COUNT(m.mission_id)
          FROM missions m
          WHERE m.tenant_id = :tenantId
            AND (
              :search IS NULL
              OR LOWER(m.title) LIKE LOWER(CONCAT('%', :search, '%'))
            )
      """, nativeQuery = true)
  Page<DropdownOptionDTO> searchForDropdown(
      @Param("search") String search,
      @Param("tenantId") String tenantId,
      Pageable pageable);
}