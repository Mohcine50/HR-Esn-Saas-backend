package com.shegami.hr_saas.modules.timesheet.dto;

import java.time.LocalDateTime;

public record TimesheetExportRow(
        String timesheetId,
        String consultantId,
        String consultantName,
        String missionId,
        String missionTitle,
        int month,
        int year,
        double totalDays,
        LocalDateTime validatedAt,
        String validatedBy
) {}