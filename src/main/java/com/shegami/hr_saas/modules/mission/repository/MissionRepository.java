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

    @Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto(CAST(m.status AS string), COUNT(m)) FROM Mission m WHERE m.tenant.tenantId = :tenantId GROUP BY m.status")
    java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto> countByStatusAndTenantId(
            @Param("tenantId") String tenantId);

    long countByTenantTenantIdAndStatusIn(String tenantId,
            java.util.List<com.shegami.hr_saas.modules.mission.enums.MissionStatus> statuses);

    @Query("SELECT m FROM Mission m WHERE m.accountManager.employeeId = :employeeId AND m.tenant.tenantId = :tenantId")
    java.util.List<Mission> findByAccountManagerAndTenantId(@Param("employeeId") String employeeId,
            @Param("tenantId") String tenantId);

    @Query("SELECT m FROM Mission m WHERE :consultant MEMBER OF m.consultants AND m.status IN :statuses AND m.tenant.tenantId = :tenantId")
    java.util.List<Mission> findByConsultantAndStatusInAndTenantId(@Param("consultant") Consultant consultant,
            @Param("statuses") java.util.List<com.shegami.hr_saas.modules.mission.enums.MissionStatus> statuses,
            @Param("tenantId") String tenantId);

    @Query("SELECT m FROM Mission m WHERE m.accountManager.employeeId = :employeeId AND m.tenant.tenantId = :tenantId AND m.status IN :statuses")
    java.util.List<Mission> findByAccountManagerAndStatusInAndTenantId(@Param("employeeId") String employeeId,
            @Param("statuses") java.util.List<com.shegami.hr_saas.modules.mission.enums.MissionStatus> statuses,
            @Param("tenantId") String tenantId);

    @Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto(CAST(m.status AS string), COUNT(m)) FROM Mission m WHERE m.accountManager.employeeId = :employeeId AND m.tenant.tenantId = :tenantId GROUP BY m.status")
    java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto> countByStatusAndAccountManagerAndTenantId(
            @Param("employeeId") String employeeId, @Param("tenantId") String tenantId);

    @Query("SELECT m FROM Mission m WHERE m.endDate BETWEEN :startDate AND :endDate AND :consultant MEMBER OF m.consultants AND m.tenant.tenantId = :tenantId")
    java.util.List<Mission> findUpcomingDeadlinesForConsultant(@Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate, @Param("consultant") Consultant consultant,
            @Param("tenantId") String tenantId);

    @Query("SELECT m FROM Mission m WHERE m.endDate BETWEEN :startDate AND :endDate AND m.accountManager.employeeId = :employeeId AND m.tenant.tenantId = :tenantId")
    java.util.List<Mission> findUpcomingDeadlinesForManager(@Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate, @Param("employeeId") String employeeId,
            @Param("tenantId") String tenantId);

    long countByTenantTenantIdAndStatus(String tenantId,
            com.shegami.hr_saas.modules.mission.enums.MissionStatus status);

    // ── Analytics / Reports queries ──────────────────────────────────────

    @Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto(CAST(m.priority AS string), COUNT(m)) FROM Mission m WHERE m.tenant.tenantId = :tenantId GROUP BY m.priority")
    java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.CountByStatusDto> countByPriorityAndTenantId(
            @Param("tenantId") String tenantId);

    @Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyCountDto(MONTH(m.startDate), YEAR(m.startDate), COUNT(m)) FROM Mission m WHERE m.tenant.tenantId = :tenantId AND m.startDate >= :startDate GROUP BY YEAR(m.startDate), MONTH(m.startDate) ORDER BY YEAR(m.startDate) ASC, MONTH(m.startDate) ASC")
    java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.MonthlyCountDto> countStartedByMonthAndTenantId(
            @Param("startDate") java.time.LocalDate startDate, @Param("tenantId") String tenantId);

    @Query("SELECT new com.shegami.hr_saas.modules.reporting.dto.shared.ClientMissionCountDto(c.fullName, COUNT(m)) FROM Mission m JOIN m.client c WHERE m.tenant.tenantId = :tenantId GROUP BY c.fullName ORDER BY COUNT(m) DESC")
    java.util.List<com.shegami.hr_saas.modules.reporting.dto.shared.ClientMissionCountDto> countByClientAndTenantId(
            @Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT m FROM Mission m WHERE m.tenant.tenantId = :tenantId")
    java.util.List<Mission> findAllByTenantId(@Param("tenantId") String tenantId);
}