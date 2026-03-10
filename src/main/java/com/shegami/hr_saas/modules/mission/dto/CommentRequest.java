package com.shegami.hr_saas.modules.mission.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank(message = "Comment content is required")
        String content
) {}