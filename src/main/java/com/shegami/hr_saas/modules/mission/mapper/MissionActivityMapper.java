package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.mission.dto.MissionActivityResponse;
import com.shegami.hr_saas.modules.mission.entity.MissionActivity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MissionActivityMapper {
    MissionActivity toEntity(MissionActivityResponse missionActivityResponse);

    MissionActivityResponse toResponse(MissionActivity missionActivity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MissionActivity partialUpdate(MissionActivityResponse missionActivityResponse, @MappingTarget MissionActivity missionActivity);
}