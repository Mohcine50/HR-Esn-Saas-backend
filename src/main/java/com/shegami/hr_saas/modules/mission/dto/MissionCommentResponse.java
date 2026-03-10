package com.shegami.hr_saas.modules.mission.dto;

import java.time.LocalDateTime;

public record MissionCommentResponse(
        String   commentId,
        String   content,
        String   authorId,
        String   authorName,
        boolean  edited,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
