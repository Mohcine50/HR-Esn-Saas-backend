package com.shegami.hr_saas.modules.reporting.dto.shared;

public record TimesheetApprovalDto(
        String timesheetId, String consultantName,
        String missionTitle, int month, int year) {
}
