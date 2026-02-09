package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.modules.mission.dto.CreateProjectRequest;
import com.shegami.hr_saas.modules.mission.dto.ProjectDto;
import com.shegami.hr_saas.modules.mission.dto.UpdateProjectRequest;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import com.shegami.hr_saas.modules.mission.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class ProjectServiceImpl implements ProjectService {
    @Override
    public ProjectDto createProject(CreateProjectRequest request) {
        return null;
    }

    @Override
    public ProjectDto updateProject(String projectId, UpdateProjectRequest request) {
        return null;
    }

    @Override
    public ProjectDto getProjectById(String projectId) {
        return null;
    }

    @Override
    public Page<ProjectDto> getProjects(Pageable pageable) {
        return null;
    }

    @Override
    public Page<ProjectDto> searchProjects(String keyword, Pageable pageable) {
        return null;
    }

    @Override
    public void changeProjectStatus(String projectId, ProjectStatus status) {

    }

    @Override
    public void assignUserToProject(String projectId, Long userId) {

    }

    @Override
    public void removeUserFromProject(String projectId, Long userId) {

    }

    @Override
    public void deleteProject(String projectId) {

    }
}
