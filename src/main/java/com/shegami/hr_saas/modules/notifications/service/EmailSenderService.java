package com.shegami.hr_saas.modules.notifications.service;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

public interface EmailSenderService {

    public void sendEmail();

    void sendInvitationEmail(String to, String invitationLink) throws MessagingException;
}
