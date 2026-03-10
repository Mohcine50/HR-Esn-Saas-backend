package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.CommentRequest;
import com.shegami.hr_saas.modules.mission.dto.MissionCommentResponse;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MissionCommentService {
    @Transactional
    MissionCommentResponse addComment(String missionId, CommentRequest req);

    @Transactional
    MissionCommentResponse editComment(String commentId, CommentRequest req);

    @Transactional
    void deleteComment(String commentId);

    @Transactional(readOnly = true)
    List<MissionCommentResponse> getComments(String missionId);
}
