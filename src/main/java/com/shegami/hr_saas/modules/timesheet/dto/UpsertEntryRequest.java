package com.shegami.hr_saas.modules.timesheet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpsertEntryRequest(
        @NotNull LocalDate date,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double quantity,
        String comment
) {}