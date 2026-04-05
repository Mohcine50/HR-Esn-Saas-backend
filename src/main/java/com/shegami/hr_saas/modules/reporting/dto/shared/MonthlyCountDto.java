package com.shegami.hr_saas.modules.reporting.dto.shared;

public record MonthlyCountDto(
        int month,
        int year,
        long count) {
}
