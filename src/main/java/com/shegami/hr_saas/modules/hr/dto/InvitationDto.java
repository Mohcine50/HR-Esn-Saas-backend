package com.shegami.hr_saas.modules.hr.dto;

import com.shegami.hr_saas.modules.auth.dto.TenantDto;
import com.shegami.hr_saas.modules.auth.dto.UserDto;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.hr.enums.InvitationStatus;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.shegami.hr_saas.modules.hr.entity.Invitation}
 */
@Value
public class InvitationDto implements Serializable {
    TenantDto tenant;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String invitationId;
    String enviteeEmail;
    String invitationToken;
    InvitationStatus status;
    LocalDateTime invitedAt;
    LocalDateTime acceptedAt;
    UserDto inviter;
    UserRoles userRole;
    String firstName;
    String lastName;
}