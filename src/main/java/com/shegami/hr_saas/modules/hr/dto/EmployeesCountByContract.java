package com.shegami.hr_saas.modules.hr.dto;

import com.shegami.hr_saas.modules.hr.enums.ContractType;

public record EmployeesCountByContract(
        ContractType contractType,
        Long count
) {}