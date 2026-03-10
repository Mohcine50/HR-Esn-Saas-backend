package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.mission.dto.CommentRequest;
import com.shegami.hr_saas.modules.mission.dto.MissionCommentResponse;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.entity.MissionComment;
import com.shegami.hr_saas.modules.mission.enums.ActivityType;
import com.shegami.hr_saas.modules.mission.exceptions.MissionNotFoundException;
import com.shegami.hr_saas.modules.mission.mapper.MissionCommentMapper;
import com.shegami.hr_saas.modules.mission.repository.MissionCommentRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.service.MissionActivityService;
import com.shegami.hr_saas.modules.mission.service.MissionCommentService;

import com.shegami.hr_saas.shared.exception.ApiRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionCommentServiceImpl implements MissionCommentService {
    private final MissionCommentRepository commentRepository;
    private final MissionRepository missionRepository;
    private final MissionActivityService activityService;
    private final MissionCommentMapper missionCommentMapper;


    @Transactional
    @Override
    public MissionCommentResponse addComment(String missionId, CommentRequest req) {
        String tenantId  = UserContextHolder.getCurrentUserContext().tenantId();
        String userId    = UserContextHolder.getCurrentUserContext().userId();
        String userName  = UserContextHolder.getCurrentUserContext().email(); // or full name if available

        log.info("[Comment] Adding comment | missionId={} authorId={}", missionId, userId);

        Mission mission = missionRepository
                .findByMissionIdAndTenantTenantId(missionId, tenantId)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found: " + missionId));

        MissionComment comment = new MissionComment();
        comment.setMission(mission);
        comment.setContent(req.content());
        comment.setAuthorId(userId);
        comment.setAuthorName(userName);
        comment.setTenant(mission.getTenant());

        MissionComment saved = commentRepository.save(comment);

        // log activity
        activityService.log(mission, ActivityType.COMMENT_ADDED,
                "added a comment", userId, userName);

        log.info("[Comment] Comment added | commentId={} missionId={}", saved.getCommentId(), missionId);
        return missionCommentMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public MissionCommentResponse editComment(String commentId, CommentRequest req) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId   = UserContextHolder.getCurrentUserContext().userId();

        log.info("[Comment] Editing comment | commentId={} editorId={}", commentId, userId);

        MissionComment comment = commentRepository
                .findByCommentIdAndTenantTenantId(commentId, tenantId)
                .orElseThrow(() -> new ApiRequestException("Comment not found: " + commentId));

        // Only the author can edit
        if (!comment.getAuthorId().equals(userId)) {
            throw new ApiRequestException("You can only edit your own comments.");
        }

        comment.setContent(req.content());
        comment.setEdited(true);

        activityService.log(comment.getMission(), ActivityType.COMMENT_EDITED,
                "edited a comment", userId, comment.getAuthorName());

        return missionCommentMapper.toResponse(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public void deleteComment(String commentId) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId   = UserContextHolder.getCurrentUserContext().userId();

        log.info("[Comment] Deleting comment | commentId={} deletorId={}", commentId, userId);

        MissionComment comment = commentRepository
                .findByCommentIdAndTenantTenantId(commentId, tenantId)
                .orElseThrow(() -> new ApiRequestException("Comment not found: " + commentId));

        if (!comment.getAuthorId().equals(userId)) {
            throw new ApiRequestException("You can only delete your own comments.");
        }

        activityService.log(comment.getMission(), ActivityType.COMMENT_DELETED,
                "deleted a comment", userId, comment.getAuthorName());

        commentRepository.delete(comment);
        log.info("[Comment] Comment deleted | commentId={}", commentId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<MissionCommentResponse> getComments(String missionId) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return commentRepository
                .findByMissionMissionIdAndTenantTenantIdOrderByCreatedAtDesc(missionId, tenantId)
                .stream()
                .map(missionCommentMapper::toResponse)
                .toList();
    }



}
