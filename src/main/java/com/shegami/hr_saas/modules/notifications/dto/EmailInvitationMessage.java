package com.shegami.hr_saas.modules.notifications.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailInvitationMessage extends BaseMessage {
    private String invitationId;
    private String recipientEmail;
    private String recipientFirstName;
    private String recipientLastName;
    private String inviterName;
    private String invitationToken;
    private String role;
    private String companyName;
    private Map<String, Object> metadata; // position, department, etc.
}