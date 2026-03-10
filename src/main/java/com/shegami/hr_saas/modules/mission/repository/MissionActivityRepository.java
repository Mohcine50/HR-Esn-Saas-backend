package com.shegami.hr_saas.modules.mission.repository;


import com.shegami.hr_saas.modules.mission.entity.MissionActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionActivityRepository extends JpaRepository<MissionActivity, String> {

    List<MissionActivity> findByMissionMissionIdAndTenantTenantIdOrderByCreatedAtDesc(
            String missionId, String tenantId
    );
}