package com.shegami.hr_saas.modules.billing.service;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.timesheet.dto.TimesheetApprovedEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.transaction.annotation.Transactional;

public interface InvoiceService {
    @RabbitListener(queues = RabbitMQConfig.BILLING_QUEUE)
    @Transactional
    void handleTimesheetApproved(TimesheetApprovedEvent event);
}
