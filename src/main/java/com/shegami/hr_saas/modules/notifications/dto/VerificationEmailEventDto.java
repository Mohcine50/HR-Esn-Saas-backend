package com.shegami.hr_saas.modules.notifications.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
public class VerificationEmailEventDto {

    private String eventId;
    private String userEmail;
    private String verificationToken;
    private LocalDateTime invitationDate;
}
