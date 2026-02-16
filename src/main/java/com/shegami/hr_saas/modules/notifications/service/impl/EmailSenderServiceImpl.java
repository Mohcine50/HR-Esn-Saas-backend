package com.shegami.hr_saas.modules.notifications.service.impl;

import com.shegami.hr_saas.modules.notifications.service.EmailSenderService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;


    @Override
    public void sendEmail() {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo("test-recive@shegami-saas.com");
            helper.setSubject("Test receive");
            helper.setText("Test receive", true);
            helper.setFrom("no-reply@shegami-saas.com");

            mailSender.send(message);

        } catch (Exception e) {
            log.error("Failed to send email ,Error: {}", e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }

    @SneakyThrows
    @Override
    public void sendInvitationEmail(String to, String invitationLink) {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("noreply@myapp.com");
        helper.setTo(to);
        helper.setSubject("You're invited!");

        helper.setText(invitationLink, true);

        mailSender.send(message);
    }

    @Override
    public void sendInvitationEmail(String recipientEmail, String recipientFirstName, String inviterName, String invitationToken, String role, String companyName, Map<String, Object> metadata) {

    }

    @Override
    public void sendEmailVerification(String recipientEmail, String recipientFirstName, String verificationToken) {

    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientFirstName, String verificationToken) {

    }

    @Override
    public void sendSecurityAlertEmail(String recipientEmail, String recipientFirstName, Map<String, Object> context) {

    }


}
