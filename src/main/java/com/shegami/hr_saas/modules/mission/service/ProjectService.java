package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.CreateProjectRequest;
import com.shegami.hr_saas.modules.mission.dto.ProjectDto;
import com.shegami.hr_saas.modules.mission.dto.UpdateProjectRequest;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {

    ProjectDto createProject(CreateProjectRequest request);

    ProjectDto updateProject(Long projectId, UpdateProjectRequest request);

    ProjectDto getProjectById(Long projectId);

    Page<ProjectDto> getProjects(Pageable pageable);

    Page<ProjectDto> searchProjects(String keyword, Pageable pageable);

    void changeProjectStatus(Long projectId, ProjectStatus status);

    void assignUserToProject(Long projectId, Long userId);

    void removeUserFromProject(Long projectId, Long userId);

    void deleteProject(Long projectId);
}

