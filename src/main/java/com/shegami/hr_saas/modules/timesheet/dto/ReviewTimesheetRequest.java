package com.shegami.hr_saas.modules.timesheet.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewTimesheetRequest(
        @NotNull boolean approved,
        String comment   // Required when rejecting, optional when approving
) {}