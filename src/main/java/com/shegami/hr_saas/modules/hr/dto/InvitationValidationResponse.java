package com.shegami.hr_saas.modules.hr.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Builder
@Getter
@Value
public class InvitationValidationResponse {
    String firstName;
    String tenantName;
}
