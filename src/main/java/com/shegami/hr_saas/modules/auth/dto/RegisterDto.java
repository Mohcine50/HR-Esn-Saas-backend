package com.shegami.hr_saas.modules.auth.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class RegisterDto {
    private String email;
    private String password;
    private String confirmPassword;
}
