package com.shegami.hr_saas.modules.mission.dto;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link com.shegami.hr_saas.modules.mission.entity.Label}
 */
@Value
public class LabelDto implements Serializable {
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String labelName;
    String labelDescription;
    String color;
    Integer missions;
    String labelId;
}