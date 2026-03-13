package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import lombok.Value;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Value
public class ProjectSummaryDto implements Serializable {
    String        projectId;
    String        name;
    String        description;
    Priority priority;
    ProjectStatus projectStatus;
    Set<String> tags;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}