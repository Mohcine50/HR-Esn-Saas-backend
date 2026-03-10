package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.mission.dto.MissionCommentResponse;
import com.shegami.hr_saas.modules.mission.entity.MissionComment;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface MissionCommentMapper {
    MissionComment toEntity(MissionCommentResponse missionCommentResponse);

    MissionCommentResponse toResponse(MissionComment missionComment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    MissionComment partialUpdate(MissionCommentResponse missionCommentResponse, @MappingTarget MissionComment missionComment);
}