package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.enums.ActivityType;

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
}
