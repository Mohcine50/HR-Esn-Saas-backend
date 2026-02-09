package com.shegami.hr_saas.modules.mission.controller;

import com.shegami.hr_saas.modules.mission.dto.CreateProjectRequest;
import com.shegami.hr_saas.modules.mission.dto.ProjectDto;
import com.shegami.hr_saas.modules.mission.dto.UpdateProjectRequest;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import com.shegami.hr_saas.modules.mission.service.ProjectService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/projects")
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto> createProject(
            @RequestBody @Valid CreateProjectRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectDto>> getProjects(
            Pageable pageable
    ) {
        return ResponseEntity.ok(projectService.getProjects(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProjectDto>> searchProjects(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                projectService.searchProjects(keyword, pageable)
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(
            @PathVariable String id,
            @RequestBody @Valid UpdateProjectRequest request
    ) {
        return ResponseEntity.ok(
                projectService.updateProject(id, request)
        );
    }

    // CHANGE STATUS (business action)
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeProjectStatus(
            @PathVariable String id,
            @RequestParam ProjectStatus status
    ) {
        projectService.changeProjectStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> assignUserToProject(
            @PathVariable String projectId,
            @PathVariable String userId
    ) {
        projectService.assignUserToProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{projectId}/users/{userId}")
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable String projectId,
            @PathVariable String userId
    ) {
        projectService.removeUserFromProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable String id
    ) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
