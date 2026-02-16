package com.shegami.hr_saas.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationMessage extends BaseMessage {
    private String userId;
    private String recipientEmail;
    private String recipientFirstName;
    private String verificationToken;
    private String verificationType; // EMAIL_VERIFICATION, PASSWORD_RESET
}