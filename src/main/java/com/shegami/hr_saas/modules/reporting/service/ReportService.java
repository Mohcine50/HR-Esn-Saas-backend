package com.shegami.hr_saas.modules.reporting.service;

import com.shegami.hr_saas.modules.reporting.dto.reports.*;

import java.time.LocalDate;

public interface ReportService {

    WorkforceReportDto getWorkforceReport(LocalDate startDate, LocalDate endDate);

    RevenueReportDto getRevenueReport(LocalDate startDate, LocalDate endDate);

    MissionReportDto getMissionReport(LocalDate startDate, LocalDate endDate);

    TimesheetReportDto getTimesheetReport(LocalDate startDate, LocalDate endDate);

    BillingReportDto getBillingReport(LocalDate startDate, LocalDate endDate);
}
