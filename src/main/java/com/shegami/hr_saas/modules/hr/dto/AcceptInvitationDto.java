package com.shegami.hr_saas.modules.hr.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@Getter
@Setter
@Value
public class AcceptInvitationDto {

    String password;
    String confirmPassword;

}
