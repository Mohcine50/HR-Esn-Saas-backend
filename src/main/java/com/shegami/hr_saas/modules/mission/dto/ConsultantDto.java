package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.mission.enums.ConsultantLevel;
import com.shegami.hr_saas.modules.mission.enums.ConsultantStatus;
import com.shegami.hr_saas.modules.mission.enums.ConsultantType;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetResponse;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO for {@link com.shegami.hr_saas.modules.mission.entity.Consultant}
 */
@Value
public class ConsultantDto implements Serializable {
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String firstName;
    String lastName;
    String email;
    String profileTitle;
    ConsultantType type;
    ConsultantStatus status;
    BigDecimal internalDailyCost;
    ConsultantLevel seniority;
    Set<String> skills;
    String cvS3Key;
    UserDto user;
    String consultantId;
    List<MissionDto> missions;
    List<TimesheetResponse> timesheets;
    List<ProjectDto> projects;

}