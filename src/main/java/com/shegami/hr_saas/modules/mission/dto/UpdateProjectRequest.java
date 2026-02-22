package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.mission.enums.Priority;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateProjectRequest {

    private String name;
    private String description;
    private Priority priority;
    private Set<String> tags;
}