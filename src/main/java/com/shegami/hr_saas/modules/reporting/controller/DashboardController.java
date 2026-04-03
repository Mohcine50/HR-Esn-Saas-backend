package com.shegami.hr_saas.modules.reporting.controller;

import com.shegami.hr_saas.modules.reporting.dto.AdminDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.ConsultantDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.FinancialDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.ManagerDashboardDto;
import com.shegami.hr_saas.modules.reporting.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Dashboard Overview per Role")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/consultant")
    @PreAuthorize("hasAnyAuthority('CONSULTANT')")
    @Operation(summary = "Get Consultant Dashboard")
    public ResponseEntity<ConsultantDashboardDto> getConsultantDashboard() {
        log.info("REST request to get Consultant Dashboard");

        ConsultantDashboardDto response = dashboardService.getConsultantDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'COMPANY_OWNER')")
    @Operation(summary = "Get Admin Dashboard", description = "Global view of the tenant")
    public ResponseEntity<AdminDashboardDto> getAdminDashboard() {
        log.info("REST request to get Admin Dashboard");

        AdminDashboardDto response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyAuthority('MANAGER', 'ADMIN', 'COMPANY_OWNER')")
    @Operation(summary = "Get Manager Dashboard", description = "View of managed missions and team")
    public ResponseEntity<ManagerDashboardDto> getManagerDashboard() {
        log.info("REST request to get Manager Dashboard");

        ManagerDashboardDto response = dashboardService.getManagerDashboard();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/financial")
    @PreAuthorize("hasAnyAuthority('FINANCIAL', 'ADMIN', 'COMPANY_OWNER')")
    @Operation(summary = "Get Financial Dashboard", description = "Billing and revenue overview")
    public ResponseEntity<FinancialDashboardDto> getFinancialDashboard() {
        log.info("REST request to get Financial Dashboard");

        FinancialDashboardDto response = dashboardService.getFinancialDashboard();
        return ResponseEntity.ok(response);
    }
}
