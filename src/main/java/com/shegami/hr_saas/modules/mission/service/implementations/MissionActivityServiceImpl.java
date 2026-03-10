package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.entity.MissionActivity;
import com.shegami.hr_saas.modules.mission.enums.ActivityType;
import com.shegami.hr_saas.modules.mission.repository.MissionActivityRepository;
import com.shegami.hr_saas.modules.mission.service.MissionActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class MissionActivityServiceImpl implements MissionActivityService {
    private final MissionActivityRepository activityRepository;

    @Override
    public void log(
            Mission mission,
            ActivityType type,
            String field,
            String fromValue,
            String toValue,
            String actorId,
            String actorName
    ) {
        MissionActivity activity = new MissionActivity();
        activity.setMission(mission);
        activity.setType(type);
        activity.setField(field);
        activity.setFromValue(fromValue);
        activity.setToValue(toValue);
        activity.setActorId(actorId);
        activity.setActorName(actorName);
        activity.setDescription(buildDescription(type, field, fromValue, toValue));
        activity.setTenant(mission.getTenant());

        activityRepository.save(activity);
        log.debug("[Activity] Logged | missionId={} type={} actor={}",
                mission.getMissionId(), type, actorName);
    }

    // for actions with no from/to values
    @Override
    public void log(
            Mission mission,
            ActivityType type,
            String description,
            String actorId,
            String actorName
    ) {
        MissionActivity activity = new MissionActivity();
        activity.setMission(mission);
        activity.setType(type);
        activity.setDescription(description);
        activity.setActorId(actorId);
        activity.setActorName(actorName);
        activity.setTenant(mission.getTenant());

        activityRepository.save(activity);
    }

    private String buildDescription(
            ActivityType type, String field, String from, String to
    ) {
        return switch (type) {
            case STATUS_CHANGED    -> "changed status from %s to %s".formatted(from, to);
            case PRIORITY_CHANGED  -> "changed priority from %s to %s".formatted(from, to);
            case TITLE_CHANGED     -> "updated title";
            case CONSULTANT_ASSIGNED -> "assigned consultant %s".formatted(to);
            case CONSULTANT_REMOVED  -> "removed consultant %s".formatted(from);
            case LABEL_ADDED       -> "added label %s".formatted(to);
            case LABEL_REMOVED     -> "removed label %s".formatted(from);
            case PROJECT_ASSIGNED  -> "assigned to project %s".formatted(to);
            case COMMENT_ADDED     -> "added a comment";
            case COMMENT_EDITED    -> "edited a comment";
            case COMMENT_DELETED   -> "deleted a comment";
            default                -> type.name().toLowerCase().replace("_", " ");
        };
    }
}