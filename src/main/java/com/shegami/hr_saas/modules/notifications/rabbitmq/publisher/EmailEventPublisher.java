package com.shegami.hr_saas.modules.notifications.rabbitmq.publisher;

import com.shegami.hr_saas.modules.notifications.dto.VerificationEmailEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static com.shegami.hr_saas.shared.constants.EmailConstant.EXCHANGE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void sendInvitationEmail(VerificationEmailEventDto event) {
        log.info("Publishing invitation event for: {}", event.getUserEmail());

        String specificRoutingKey = "notification.email.invitation";

        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                specificRoutingKey,
                event
        );
    }
}