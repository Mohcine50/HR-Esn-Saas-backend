package com.shegami.hr_saas.modules.mission.controller;

import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.dto.NewMissionRequest;
import com.shegami.hr_saas.modules.mission.service.MissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Slf4j
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    public ResponseEntity<MissionDto> create(@Valid @RequestBody NewMissionRequest dto) {
        return new ResponseEntity<>(missionService.createMission(dto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<MissionDto>> getAll(
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(missionService.getMissionsByTenant(pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<MissionDto>> getMissionsForConsultant(
            @PageableDefault(size = 10) Pageable pageable
    ){
        return ResponseEntity.ok(missionService.getMissionByConsultant(pageable));
    }

    @PatchMapping("/{id}/terminate")
    public ResponseEntity<Void> terminate(@PathVariable String id) {
        missionService.terminateMission(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{missionId}/assign/{consultantId}")
    public ResponseEntity<Void> assign(@PathVariable String missionId, @PathVariable String consultantId) {
        missionService.assignConsultantToMission(missionId, consultantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionDto> getMissionById(@PathVariable String id) {
        log.info("REST request to get Mission : {}", id);
        MissionDto missionDto = missionService.getMissionById(id);
        return ResponseEntity.ok(missionDto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<MissionDto> updateMission(
            @PathVariable String id,
            @Valid @RequestBody MissionDto missionDto) {
        log.info("REST request to update Mission : {}", id);

        MissionDto result = missionService.updateMission(missionDto, id);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable String id) {
        log.info("REST request to delete Mission : {}", id);
        missionService.deleteMission(id);

        return ResponseEntity.noContent().build();
    }
}
