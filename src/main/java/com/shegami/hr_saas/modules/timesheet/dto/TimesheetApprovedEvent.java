package com.shegami.hr_saas.modules.timesheet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimesheetApprovedEvent {
    private String timesheetId;
    private String tenantId;
    private String missionId;
}
