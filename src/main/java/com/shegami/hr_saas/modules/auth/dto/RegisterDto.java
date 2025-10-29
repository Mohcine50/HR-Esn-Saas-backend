package com.shegami.hr_saas.modules.auth.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RegisterDto {

    // User Details
    private String email;
    private String password;
    private String confirmPassword;

    private String firstName;
    private String lastName;
    private String phone;

    // Tenant details
    private String companyName;
    private String CompanyDomain;



}
