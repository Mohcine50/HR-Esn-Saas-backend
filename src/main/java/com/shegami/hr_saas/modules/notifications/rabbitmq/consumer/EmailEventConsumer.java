package com.shegami.hr_saas.modules.notifications.rabbitmq.consumer;

import com.shegami.hr_saas.modules.notifications.dto.VerificationEmailEventDto;
import com.shegami.hr_saas.modules.notifications.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.shegami.hr_saas.shared.constants.EmailConstant.QUEUE_NAME;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventConsumer {

    private final EmailSenderService emailSenderService;

    @RabbitListener(queues = QUEUE_NAME)
    public void consumeVerificationEvent(VerificationEmailEventDto event) {
        log.info("Received Message: {}", event);

        try {
            /*emailSenderService.sendEmail(
                    event.getUserEmail(),
                    "Please verify: " + event.getVerificationUrl()
            );*/
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}. Error: {}", event.getUserEmail(), e.getMessage());
            throw e;
        }
    }
}