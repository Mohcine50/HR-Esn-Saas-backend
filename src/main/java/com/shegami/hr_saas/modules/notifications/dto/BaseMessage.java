package com.shegami.hr_saas.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseMessage {
    private String messageId;
    private String tenantId;
    private LocalDateTime createdAt;
    private int retryCount;

    public BaseMessage(String tenantId) {
        this.tenantId = tenantId;
        this.createdAt = LocalDateTime.now();
        this.retryCount = 0;
        this.messageId = UUID.randomUUID().toString();
    }
}
