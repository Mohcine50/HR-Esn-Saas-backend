package com.shegami.hr_saas.modules.reporting.dto.shared;

public record MonthlyUtilizationDto(
        int month,
        int year,
        double utilizationRate) {
}
