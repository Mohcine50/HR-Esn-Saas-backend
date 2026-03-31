package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.mission.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Client is required")
    private String clientId;

    private Set<String> tags;
}