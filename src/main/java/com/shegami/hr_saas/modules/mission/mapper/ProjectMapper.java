package com.shegami.hr_saas.modules.mission.mapper;

import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.mission.dto.ProjectDto;
import com.shegami.hr_saas.modules.mission.dto.UpdateProjectRequest;
import com.shegami.hr_saas.modules.mission.entity.Project;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {UserMapper.class, UserMapper.class, ConsultantMapper.class, MissionMapper.class})
public interface ProjectMapper {
    Project toEntity(ProjectDto projectDto);

    ProjectDto toDto(Project project);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Project partialUpdate(UpdateProjectRequest projectDto, @MappingTarget Project project);
}