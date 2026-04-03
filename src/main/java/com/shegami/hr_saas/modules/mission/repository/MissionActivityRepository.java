package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.MissionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionActivityRepository extends JpaRepository<MissionActivity, String> {

    List<MissionActivity> findByMissionMissionIdAndTenantTenantIdOrderByCreatedAtDesc(
            String missionId, String tenantId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM MissionActivity a WHERE a.tenant.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<MissionActivity> findRecentByTenantId(
            @org.springframework.data.repository.query.Param("tenantId") String tenantId,
            org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM MissionActivity a WHERE a.mission.accountManager.employeeId = :employeeId AND a.tenant.tenantId = :tenantId ORDER BY a.createdAt DESC")
    List<MissionActivity> findRecentByManagerAndTenantId(
            @org.springframework.data.repository.query.Param("employeeId") String employeeId,
            @org.springframework.data.repository.query.Param("tenantId") String tenantId,
            org.springframework.data.domain.Pageable pageable);
}