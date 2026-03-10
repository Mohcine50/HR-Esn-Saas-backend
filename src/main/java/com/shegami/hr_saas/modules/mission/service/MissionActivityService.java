package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.MissionActivityResponse;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.enums.ActivityType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MissionActivityService {
    void log(
            Mission mission,
            ActivityType type,
            String field,
            String fromValue,
            String toValue,
            String actorId,
            String actorName
    );

    // for actions with no from/to values
    void log(
            Mission mission,
            ActivityType type,
            String description,
            String actorId,
            String actorName
    );

    @Transactional(readOnly = true)
    List<MissionActivityResponse> getActivities(String missionId);
}
