package com.shegami.hr_saas.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class RegisterResponseDto{
    private String token,  message;
}
