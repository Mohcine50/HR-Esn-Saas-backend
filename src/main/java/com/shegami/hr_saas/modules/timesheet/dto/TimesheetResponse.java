package com.shegami.hr_saas.modules.timesheet.dto;

import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.timesheet.enums.TimesheetStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TimesheetResponse(
        String timesheetId,
        String missionId,
        String missionTitle,
        String consultantId,
        int month,
        int year,
        TimesheetStatus status,
        List<TimesheetEntryResponse> entries,
        double totalDays,
        LocalDateTime validatedAt,
        EmployeeDto validatedBy
) {}
