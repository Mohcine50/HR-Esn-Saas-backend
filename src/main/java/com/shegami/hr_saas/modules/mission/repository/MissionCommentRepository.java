package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.MissionComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MissionCommentRepository extends JpaRepository<MissionComment, String> {

    List<MissionComment> findByMissionMissionIdAndTenantTenantIdOrderByCreatedAtDesc(
            String missionId, String tenantId
    );

    Optional<MissionComment> findByCommentIdAndTenantTenantId(
            String commentId, String tenantId
    );
}