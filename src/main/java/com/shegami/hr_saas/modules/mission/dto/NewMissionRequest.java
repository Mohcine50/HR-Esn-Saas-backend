package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Set;

@Data
@AllArgsConstructor
@Getter
public class NewMissionRequest {
    private String title;
    private String description;
    private String client;
    private String project;
    private Set<String> consultants;
    private Priority priority;
    private MissionStatus status;
    private Set<String> labels;
    private Set<String> attachements;
}
