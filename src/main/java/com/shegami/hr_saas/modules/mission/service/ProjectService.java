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

    ProjectDto updateProject(String projectId, UpdateProjectRequest request);

    ProjectDto getProjectById(String projectId);

    Page<ProjectDto> getProjects(Pageable pageable);

    Page<ProjectDto> searchProjects(String keyword, Pageable pageable);

    void changeProjectStatus(String projectId, ProjectStatus status);

    void assignUserToProject(String projectId, Long userId);

    void removeUserFromProject(String projectId, Long userId);

    void deleteProject(String projectId);
}

