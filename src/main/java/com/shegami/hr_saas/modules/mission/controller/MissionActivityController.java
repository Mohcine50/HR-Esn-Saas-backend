package com.shegami.hr_saas.modules.mission.controller;

import com.shegami.hr_saas.modules.mission.dto.MissionActivityResponse;
import com.shegami.hr_saas.modules.mission.entity.MissionActivity;
import com.shegami.hr_saas.modules.mission.service.MissionActivityService;
import com.shegami.hr_saas.modules.mission.service.MissionCommentService;
import com.shegami.hr_saas.modules.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/missions/{missionId}/activities")
@RequiredArgsConstructor
public class MissionActivityController {

    private final MissionActivityService missionActivityService;

    @GetMapping
    public ResponseEntity<List<MissionActivityResponse>> getActivities(
            @PathVariable String missionId
    ) {
        return ResponseEntity.ok(missionActivityService.getActivities(missionId));
    }
}