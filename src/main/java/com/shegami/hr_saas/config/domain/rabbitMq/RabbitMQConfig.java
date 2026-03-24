package com.shegami.hr_saas.config.domain.rabbitMq;



import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * RabbitMQ Configuration

 * 1. IN_APP_NOTIFICATIONS - For real-time in-app notifications (WebSocket delivery)
 * 2. EMAIL_INVITATIONS - For user invitation emails
 * 3. EMAIL_VERIFICATIONS - For email verification/confirmation emails
 * 4. EMAIL_CRITICAL - For critical email notifications (password reset, security alerts)

 * Each has:
 * - Main exchange (Topic exchange for flexible routing)
 * - Main queue (with DLQ configured)
 * - Dead Letter Queue (for failed messages)
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    // ==================== IN-APP NOTIFICATIONS ====================

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.#";
    public static final String NOTIFICATION_DLQ_EXCHANGE = "notification.dlq.exchange";
    public static final String NOTIFICATION_DLQ_QUEUE = "notification.dlq.queue";
    public static final String NOTIFICATION_DLQ_ROUTING_KEY = "notification.dlq";

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public TopicExchange notificationDlqExchange() {
        return new TopicExchange(NOTIFICATION_DLQ_EXCHANGE);
    }

    @Bean
    public Queue notificationDlqQueue() {
        return QueueBuilder.durable(NOTIFICATION_DLQ_QUEUE).build();
    }

    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlqQueue())
                .to(notificationDlqExchange())
                .with(NOTIFICATION_DLQ_ROUTING_KEY);
    }

    // ==================== EMAIL INVITATIONS ====================

    public static final String EMAIL_INVITATION_EXCHANGE = "email.invitation.exchange";
    public static final String EMAIL_INVITATION_QUEUE = "email.invitation.queue";
    public static final String EMAIL_INVITATION_ROUTING_KEY = "email.invitation.#";
    public static final String EMAIL_INVITATION_DLQ_EXCHANGE = "email.invitation.dlq.exchange";
    public static final String EMAIL_INVITATION_DLQ_QUEUE = "email.invitation.dlq.queue";
    public static final String EMAIL_INVITATION_DLQ_ROUTING_KEY = "email.invitation.dlq";

    @Bean
    public TopicExchange emailInvitationExchange() {
        return new TopicExchange(EMAIL_INVITATION_EXCHANGE);
    }

    @Bean
    public Queue emailInvitationQueue() {
        return QueueBuilder.durable(EMAIL_INVITATION_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_INVITATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_INVITATION_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 3600000)
                .build();
    }

    @Bean
    public Binding emailInvitationBinding() {
        return BindingBuilder.bind(emailInvitationQueue())
                .to(emailInvitationExchange())
                .with(EMAIL_INVITATION_ROUTING_KEY);
    }

    @Bean
    public TopicExchange emailInvitationDlqExchange() {
        return new TopicExchange(EMAIL_INVITATION_DLQ_EXCHANGE);
    }

    @Bean
    public Queue emailInvitationDlqQueue() {
        return QueueBuilder.durable(EMAIL_INVITATION_DLQ_QUEUE).build();
    }

    @Bean
    public Binding emailInvitationDlqBinding() {
        return BindingBuilder.bind(emailInvitationDlqQueue())
                .to(emailInvitationDlqExchange())
                .with(EMAIL_INVITATION_DLQ_ROUTING_KEY);
    }

    // ==================== EMAIL VERIFICATIONS ====================

    public static final String EMAIL_VERIFICATION_EXCHANGE = "email.verification.exchange";
    public static final String EMAIL_VERIFICATION_QUEUE = "email.verification.queue";
    public static final String EMAIL_VERIFICATION_ROUTING_KEY = "email.verification.#";
    public static final String EMAIL_VERIFICATION_DLQ_EXCHANGE = "email.verification.dlq.exchange";
    public static final String EMAIL_VERIFICATION_DLQ_QUEUE = "email.verification.dlq.queue";
    public static final String EMAIL_VERIFICATION_DLQ_ROUTING_KEY = "email.verification.dlq";

    @Bean
    public TopicExchange emailVerificationExchange() {
        return new TopicExchange(EMAIL_VERIFICATION_EXCHANGE);
    }

    @Bean
    public Queue emailVerificationQueue() {
        return QueueBuilder.durable(EMAIL_VERIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_VERIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_VERIFICATION_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 1800000)
                .build();
    }

    @Bean
    public Binding emailVerificationBinding() {
        return BindingBuilder.bind(emailVerificationQueue())
                .to(emailVerificationExchange())
                .with(EMAIL_VERIFICATION_ROUTING_KEY);
    }

    @Bean
    public TopicExchange emailVerificationDlqExchange() {
        return new TopicExchange(EMAIL_VERIFICATION_DLQ_EXCHANGE);
    }

    @Bean
    public Queue emailVerificationDlqQueue() {
        return QueueBuilder.durable(EMAIL_VERIFICATION_DLQ_QUEUE).build();
    }

    @Bean
    public Binding emailVerificationDlqBinding() {
        return BindingBuilder.bind(emailVerificationDlqQueue())
                .to(emailVerificationDlqExchange())
                .with(EMAIL_VERIFICATION_DLQ_ROUTING_KEY);
    }

    // ==================== EMAIL CRITICAL (High Priority) ====================

    public static final String EMAIL_CRITICAL_EXCHANGE = "email.critical.exchange";
    public static final String EMAIL_CRITICAL_QUEUE = "email.critical.queue";
    public static final String EMAIL_CRITICAL_ROUTING_KEY = "email.critical.#";
    public static final String EMAIL_CRITICAL_DLQ_EXCHANGE = "email.critical.dlq.exchange";
    public static final String EMAIL_CRITICAL_DLQ_QUEUE = "email.critical.dlq.queue";
    public static final String EMAIL_CRITICAL_DLQ_ROUTING_KEY = "email.critical.dlq";

    @Bean
    public TopicExchange emailCriticalExchange() {
        return new TopicExchange(EMAIL_CRITICAL_EXCHANGE);
    }

    @Bean
    public Queue emailCriticalQueue() {
        return QueueBuilder.durable(EMAIL_CRITICAL_QUEUE)
                .withArgument("x-dead-letter-exchange", EMAIL_CRITICAL_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", EMAIL_CRITICAL_DLQ_ROUTING_KEY)
                .withArgument("x-max-priority", 10)
                .withArgument("x-message-ttl", 600000)
                .build();
    }

    @Bean
    public Binding emailCriticalBinding() {
        return BindingBuilder.bind(emailCriticalQueue())
                .to(emailCriticalExchange())
                .with(EMAIL_CRITICAL_ROUTING_KEY);
    }

    @Bean
    public TopicExchange emailCriticalDlqExchange() {
        return new TopicExchange(EMAIL_CRITICAL_DLQ_EXCHANGE);
    }

    @Bean
    public Queue emailCriticalDlqQueue() {
        return QueueBuilder.durable(EMAIL_CRITICAL_DLQ_QUEUE).build();
    }

    @Bean
    public Binding emailCriticalDlqBinding() {
        return BindingBuilder.bind(emailCriticalDlqQueue())
                .to(emailCriticalDlqExchange())
                .with(EMAIL_CRITICAL_DLQ_ROUTING_KEY);
    }

    // ==================== BILLING EVENTS ====================

    public static final String BILLING_EXCHANGE = "billing.exchange";
    public static final String BILLING_QUEUE = "billing.queue";
    public static final String BILLING_ROUTING_KEY = "billing.#";
    public static final String BILLING_DLQ_EXCHANGE = "billing.dlq.exchange";
    public static final String BILLING_DLQ_QUEUE = "billing.dlq.queue";
    public static final String BILLING_DLQ_ROUTING_KEY = "billing.dlq";

    @Bean
    public TopicExchange billingExchange() {
        return new TopicExchange(BILLING_EXCHANGE);
    }

    @Bean
    public Queue billingQueue() {
        return QueueBuilder.durable(BILLING_QUEUE)
                .withArgument("x-dead-letter-exchange", BILLING_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", BILLING_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding billingBinding() {
        return BindingBuilder.bind(billingQueue())
                .to(billingExchange())
                .with(BILLING_ROUTING_KEY);
    }

    @Bean
    public TopicExchange billingDlqExchange() {
        return new TopicExchange(BILLING_DLQ_EXCHANGE);
    }

    @Bean
    public Queue billingDlqQueue() {
        return QueueBuilder.durable(BILLING_DLQ_QUEUE).build();
    }

    @Bean
    public Binding billingDlqBinding() {
        return BindingBuilder.bind(billingDlqQueue())
                .to(billingDlqExchange())
                .with(BILLING_DLQ_ROUTING_KEY);
    }
}