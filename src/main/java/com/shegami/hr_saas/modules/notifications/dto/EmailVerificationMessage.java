package com.shegami.hr_saas.modules.notifications.dto;

import com.shegami.hr_saas.modules.notifications.enums.VerificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationMessage extends BaseMessage {
    private String userId;
    private String recipientEmail;
    private String recipientFirstName;
    private String verificationToken;
    private VerificationType verificationType;
    private String companyName;
}