package com.shegami.hr_saas.modules.mission.dto;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.shegami.hr_saas.modules.mission.entity.Mission}
 */
@Value
public class MissionDto implements Serializable {
    TenantDto tenant;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String title;
    String description;
    ClientDto client;
    ConsultantDto consultant;
    EmployeeDto accountManager;
    BigDecimal dailyRate;
    LocalDate startDate;
    LocalDate endDate;
    MissionStatus status;
    String mission_id;
}