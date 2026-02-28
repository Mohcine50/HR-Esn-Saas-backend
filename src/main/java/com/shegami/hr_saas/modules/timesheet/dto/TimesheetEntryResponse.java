package com.shegami.hr_saas.modules.timesheet.dto;

import java.time.LocalDate;

public record TimesheetEntryResponse(
        String timesheetEntryId,
        LocalDate date,
        Double quantity,
        String comment
) {}
