package com.shegami.hr_saas.modules.timesheet.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SaveEntriesRequest(
        @NotEmpty @Valid List<UpsertEntryRequest> entries
) {}