package com.shegami.hr_saas.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailCriticalMessage extends BaseMessage {
    private String userId;
    private String recipientEmail;
    private String recipientFirstName;
    private String criticalType; // PASSWORD_RESET, SECURITY_ALERT, ACCOUNT_LOCKED, etc.
    private String token;
    private Map<String, Object> context;
    private int priority;
}