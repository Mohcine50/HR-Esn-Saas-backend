package com.shegami.hr_saas.modules.reporting.service;

import com.shegami.hr_saas.modules.reporting.dto.AdminDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.ConsultantDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.FinancialDashboardDto;
import com.shegami.hr_saas.modules.reporting.dto.ManagerDashboardDto;

public interface DashboardService {
    ConsultantDashboardDto getConsultantDashboard();

    AdminDashboardDto getAdminDashboard();

    ManagerDashboardDto getManagerDashboard();

    FinancialDashboardDto getFinancialDashboard();
}
