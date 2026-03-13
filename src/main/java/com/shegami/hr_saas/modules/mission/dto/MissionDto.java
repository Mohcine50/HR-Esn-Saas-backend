package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO for {@link com.shegami.hr_saas.modules.mission.entity.Mission}
 */
@Value
@Setter
@Getter
public class MissionDto implements Serializable {
    TenantDto tenant;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String title;
    String description;
    ClientDto client;
    Set<ConsultantDto> consultants;
    EmployeeDto accountManager;
    ProjectDto project;
    Priority priority;
    MissionStatus status;
    String missionId;
    LocalDate startDate;
    LocalDate endDate;
    Set<LabelDto> labels;
}