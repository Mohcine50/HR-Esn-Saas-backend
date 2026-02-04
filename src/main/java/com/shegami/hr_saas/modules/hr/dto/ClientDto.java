package com.shegami.hr_saas.modules.hr.dto;

import com.shegami.hr_saas.modules.mission.entity.Client;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link Client}
 */
@Value
public class ClientDto implements Serializable {
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String fullName;
    String email;
    String phoneNumber;
    String address;
    String vatNumber;
    String clientId;
}