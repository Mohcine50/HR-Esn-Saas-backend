package com.shegami.hr_saas.modules.notifications.service;

import org.springframework.stereotype.Service;

public interface EmailSenderService {

    public void sendEmail(String to, String body);

}
