package com.shegami.hr_saas.modules.notifications.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;


@Data
@Builder
public class VerificationEmailEventDto {

    private String eventId;
    private String userEmail;
    private String verificationUrl;
    private long timestamp;

    public static VerificationEmailEventDto create(String email) {
        return VerificationEmailEventDto.builder()
                .eventId(UUID.randomUUID().toString())
                .userEmail(email)
                .verificationUrl(UUID.randomUUID().toString())
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
