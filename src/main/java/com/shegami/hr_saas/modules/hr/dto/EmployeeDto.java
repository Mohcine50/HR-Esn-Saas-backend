package com.shegami.hr_saas.modules.hr.dto;

import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.hr.enums.ContractType;
import com.shegami.hr_saas.modules.hr.enums.EmployeeStatus;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.shegami.hr_saas.modules.hr.entity.Employee}
 */
@Value
public class EmployeeDto implements Serializable {
    UserDto user;
    String position;
    LocalDateTime hireDate;
    Double salary;
    String currency;
    ContractType contractType;
    EmployeeStatus status;
    String skills;
    String employeeId;
}