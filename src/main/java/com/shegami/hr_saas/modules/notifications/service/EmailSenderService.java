package com.shegami.hr_saas.modules.notifications.service;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

import java.util.Map;

public interface EmailSenderService {

    public void sendEmail();

    void sendInvitationEmail(String to, String invitationLink) throws MessagingException;

    void sendInvitationEmail(String recipientEmail, String recipientFirstName, String inviterName,
            String invitationToken, String role, String companyName, Map<String, Object> metadata);

    void sendEmailVerification(String recipientEmail, String recipientFirstName, String verificationToken,
            String companyName);

    void sendPasswordResetEmail(String recipientEmail, String recipientFirstName, String verificationToken);

    void sendSecurityAlertEmail(String recipientEmail, String recipientFirstName, Map<String, Object> context);

    void sendPasswordChangedEmail(String recipientEmail, String recipientFirstName, String companyName,
            String loginUrl);

}
