package com.shegami.hr_saas.modules.notifications.service;

import com.shegami.hr_saas.modules.notifications.dto.VerificationEmailEventDto;

public interface AuthEmailService {
    public void SendRegistrationEmailVerification(VerificationEmailEventDto verificationEmailEventDto);
    public void SendPasswrodResetEmail(VerificationEmailEventDto verificationEmailEventDto);
    public void sendEmployeeInvitationEmail(VerificationEmailEventDto verificationEmailEventDto);
}
