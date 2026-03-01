package com.shegami.hr_saas.modules.timesheet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public record CreateTimesheetRequest(
        @NotBlank String missionId,
        @Min(1) @Max(12) int month,
        @Min(2000) int year
) {}
