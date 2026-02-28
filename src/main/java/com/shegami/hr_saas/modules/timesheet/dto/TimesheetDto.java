package com.shegami.hr_saas.modules.timesheet.dto;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for {@link com.shegami.hr_saas.modules.timesheet.entity.Timesheet}
 */
@Value
public class TimesheetDto implements Serializable {
    TenantDto tenant;
    UserDto createdBy;
    UserDto modifiedBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    MissionDto mission;
    Integer month;
    Integer year;
    TimesheetStatus status;
    Set<TimesheetEntryDto> entries;
    LocalDateTime validatedAt;
    String validatedBy;
    String timesheetId;
}