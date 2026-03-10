package com.shegami.hr_saas.modules.mission.controller;

import com.shegami.hr_saas.modules.mission.dto.CommentRequest;
import com.shegami.hr_saas.modules.mission.dto.MissionCommentResponse;
import com.shegami.hr_saas.modules.mission.service.MissionCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/missions/{missionId}/comments")
@RequiredArgsConstructor
public class MissionCommentController {

    private final MissionCommentService commentService;

    @GetMapping
    public ResponseEntity<List<MissionCommentResponse>> getComments(
            @PathVariable String missionId
    ) {
        return ResponseEntity.ok(commentService.getComments(missionId));
    }

    @PostMapping
    public ResponseEntity<MissionCommentResponse> addComment(
            @PathVariable String missionId,
            @Valid @RequestBody CommentRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.addComment(missionId, req));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<MissionCommentResponse> editComment(
            @PathVariable String missionId,
            @PathVariable String commentId,
            @Valid @RequestBody CommentRequest req
    ) {
        return ResponseEntity.ok(commentService.editComment(commentId, req));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String missionId,
            @PathVariable String commentId
    ) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}