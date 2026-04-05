package com.shegami.hr_saas.modules.reporting.controller;

import com.shegami.hr_saas.modules.reporting.dto.reports.*;
import com.shegami.hr_saas.modules.reporting.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reports & Analytics", description = "Analytics reports for Admin, Manager, and Financial roles")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/workforce")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMPANY_OWNER', 'MANAGER')")
    @Operation(summary = "Workforce Analytics Report", description = "Consultant utilization, distribution by status/seniority/type, bench analysis, and top skills")
    public ResponseEntity<WorkforceReportDto> getWorkforceReport(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get Workforce Report (startDate={}, endDate={})", startDate, endDate);
        return ResponseEntity.ok(reportService.getWorkforceReport(startDate, endDate));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMPANY_OWNER', 'FINANCIAL')")
    @Operation(summary = "Revenue Analytics Report", description = "Revenue trends, growth, breakdown by client and project")
    public ResponseEntity<RevenueReportDto> getRevenueReport(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get Revenue Report (startDate={}, endDate={})", startDate, endDate);
        return ResponseEntity.ok(reportService.getRevenueReport(startDate, endDate));
    }

    @GetMapping("/missions")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMPANY_OWNER', 'MANAGER')")
    @Operation(summary = "Mission Analytics Report", description = "Mission pipeline, completion rate, duration analysis, and client distribution")
    public ResponseEntity<MissionReportDto> getMissionReport(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get Mission Report (startDate={}, endDate={})", startDate, endDate);
        return ResponseEntity.ok(reportService.getMissionReport(startDate, endDate));
    }

    @GetMapping("/timesheets")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMPANY_OWNER', 'MANAGER')")
    @Operation(summary = "Timesheet Analytics Report", description = "Submission compliance, approval/rejection rates, and consultant productivity")
    public ResponseEntity<TimesheetReportDto> getTimesheetReport(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get Timesheet Report (startDate={}, endDate={})", startDate, endDate);
        return ResponseEntity.ok(reportService.getTimesheetReport(startDate, endDate));
    }

    @GetMapping("/billing")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMPANY_OWNER', 'FINANCIAL')")
    @Operation(summary = "Billing Analytics Report", description = "Invoice aging, collection rate, DSO, payment method breakdown")
    public ResponseEntity<BillingReportDto> getBillingReport(
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("REST request to get Billing Report (startDate={}, endDate={})", startDate, endDate);
        return ResponseEntity.ok(reportService.getBillingReport(startDate, endDate));
    }
}
