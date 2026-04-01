package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.exception.EmployeeNotFoundException;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.notifications.dto.NotificationMessage;
import com.shegami.hr_saas.modules.notifications.enums.EntityType;
import com.shegami.hr_saas.modules.notifications.enums.NotificationType;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionCommentServiceImpl implements MissionCommentService {
    private final MissionCommentRepository commentRepository;
    private final MissionRepository missionRepository;
    private final MissionActivityService activityService;
    private final MissionCommentMapper missionCommentMapper;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public MissionCommentResponse addComment(String missionId, CommentRequest req) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId = UserContextHolder.getCurrentUserContext().userId();

        log.info("[Comment] Adding comment | missionId={} authorId={}", missionId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[Comment] author not found | userId={}", userId);
                    return new UserNotFoundException("User not found: " + userId);
                });

        Mission mission = missionRepository
                .findByMissionIdAndTenantTenantId(missionId, tenantId)
                .orElseThrow(() -> new MissionNotFoundException("Mission not found: " + missionId));

        MissionComment comment = new MissionComment();
        comment.setMission(mission);
        comment.setContent(req.content());
        comment.setAuthorId(userId);
        comment.setAuthorName(author.getFirstName() + " " + author.getLastName());
        comment.setTenant(mission.getTenant());

        MissionComment saved = commentRepository.save(comment);

        // log activity
        activityService.log(mission, ActivityType.COMMENT_ADDED,
                "added a comment", userId, author.getFirstName() + " " + author.getLastName());

        // Notify participants
        notifyParticipants(mission, saved, userId, author.getFirstName() + " " + author.getLastName());

        log.info("[Comment] Comment added | commentId={} missionId={}", saved.getCommentId(), missionId);
        return missionCommentMapper.toResponse(saved);
    }

    private void notifyParticipants(Mission mission, MissionComment comment, String actorId, String actorName) {
        Set<String> recipientIds = new HashSet<>();

        // Add consultants
        if (mission.getConsultants() != null) {
            recipientIds.addAll(mission.getConsultants().stream()
                    .map(c -> c.getUser().getUserId())
                    .collect(Collectors.toSet()));
        }

        // Add Account Manager
        if (mission.getAccountManager() != null && mission.getAccountManager().getUser() != null) {
            recipientIds.add(mission.getAccountManager().getUser().getUserId());
        }

        // Remove the actor themselves
        recipientIds.remove(actorId);

        String preview = comment.getContent().length() > 80
                ? comment.getContent().substring(0, 77) + "..."
                : comment.getContent();

        recipientIds.forEach(recipientId -> {
            NotificationMessage msg = NotificationMessage.builder()
                    .userId(recipientId)
                    .notificationType(NotificationType.MISSION_COMMENT_ADDED)
                    .title(NotificationType.MISSION_COMMENT_ADDED.getDefaultTitle())
                    .message(String.format("%s commented on mission '%s'", actorName, mission.getTitle()))
                    .entityType(EntityType.MISSION)
                    .entityId(mission.getMissionId())
                    .actorId(actorId)
                    .actorName(actorName)
                    .metadata(Map.of(
                            "missionTitle", mission.getTitle(),
                            "missionId", mission.getMissionId(),
                            "commentId", comment.getCommentId(),
                            "commentPreview", preview))
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    "notification.mission.comment_added", msg);
        });
    }

    @Transactional
    @Override
    public MissionCommentResponse editComment(String commentId, CommentRequest req) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        String userId = UserContextHolder.getCurrentUserContext().userId();

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
        String userId = UserContextHolder.getCurrentUserContext().userId();

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
