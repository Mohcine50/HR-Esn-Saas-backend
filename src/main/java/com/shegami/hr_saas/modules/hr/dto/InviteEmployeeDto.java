package com.shegami.hr_saas.modules.hr.dto;

import com.shegami.hr_saas.modules.hr.enums.ContractType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link com.shegami.hr_saas.modules.hr.entity.Employee}
 */
@Value
public class InviteEmployeeDto implements Serializable {


    String tenantId;
    @NotNull
    @NotEmpty
    @NotBlank
    String firstName;
    @NotNull
    @NotEmpty
    @NotBlank
    String lastName;
    @NotNull
    @NotEmpty
    @NotBlank
    String email;
    @NotNull
    @NotEmpty
    @NotBlank
    String position;
    Double salary;
    @NotNull
    @NotEmpty
    @NotBlank
    String currency;
    @NotNull
    ContractType contractType;

    @NotNull
    @NotEmpty
    @NotBlank
    String roleName;
}