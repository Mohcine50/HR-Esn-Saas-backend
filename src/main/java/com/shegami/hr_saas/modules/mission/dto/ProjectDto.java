package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link com.shegami.hr_saas.modules.mission.entity.Project}
 */
@Value
public class ProjectDto implements Serializable {
    UserDto createdBy;
    UserDto modifiedBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String name;
    String description;
    Priority priority;
    Set<String> tags;
    Set<ConsultantDto> consultants;
    Set<MissionDto> missions;
    String projectId;
    ProjectStatus projectStatus;

}