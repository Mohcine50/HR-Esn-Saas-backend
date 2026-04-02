package com.shegami.hr_saas.modules.notifications.service.impl;

import com.shegami.hr_saas.modules.notifications.service.EmailSenderService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

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
    @SneakyThrows
    public void sendInvitationEmail(String recipientEmail,
            String recipientFirstName,
            String inviterName,
            String invitationToken,
            String role,
            String companyName,
            Map<String, Object> metadata) {

        Context context = new Context();
        context.setVariable("recipientFirstName", recipientFirstName);
        context.setVariable("verificationToken", invitationToken);
        String htmlContent = templateEngine.process("verification", context);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("noreply@myapp.com");
        helper.setTo(recipientEmail);
        helper.setSubject("Verify your email");
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Override
    @SneakyThrows
    public void sendEmailVerification(String recipientEmail, String recipientFirstName, String verificationToken,
            String companyName) {
        Context context = new Context();
        context.setVariable("recipientFirstName", recipientFirstName);
        context.setVariable("verificationToken", verificationToken);
        String htmlContent = templateEngine.process("verification", context);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom("noreply@myapp.com");
        helper.setTo(recipientEmail);
        helper.setSubject("Verify your email");
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String recipientEmail, String recipientFirstName, String verificationToken) {

    }

    @Override
    public void sendSecurityAlertEmail(String recipientEmail, String recipientFirstName, Map<String, Object> context) {

    }

    @Override
    @SneakyThrows
    public void sendPasswordChangedEmail(String recipientEmail, String recipientFirstName, String companyName,
            String loginUrl) {
        Context context = new Context();
        context.setVariable("recipientFirstName", recipientFirstName);
        context.setVariable("companyName", companyName);
        context.setVariable("loginUrl", loginUrl);

        String htmlContent = templateEngine.process("emails/password-changed", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("no-reply@shegami-saas.com");
        helper.setTo(recipientEmail);
        helper.setSubject("Security Update: Your password has been changed");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    @Override
    @SneakyThrows
    public void sendEmailVerifiedEmail(String recipientEmail, String recipientFirstName, String companyName,
            String loginUrl) {
        Context context = new Context();
        context.setVariable("recipientFirstName", recipientFirstName);
        context.setVariable("companyName", companyName);
        context.setVariable("loginUrl", loginUrl);

        String htmlContent = templateEngine.process("emails/email-verified", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("no-reply@shegami-saas.com");
        helper.setTo(recipientEmail);
        helper.setSubject("Account Verified Successfully!");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
