package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.dto.CreateProjectRequest;
import com.shegami.hr_saas.modules.mission.dto.ProjectDto;
import com.shegami.hr_saas.modules.mission.dto.UpdateProjectRequest;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.enums.ProjectStatus;
import com.shegami.hr_saas.modules.mission.exceptions.ClientNotFoundException;
import com.shegami.hr_saas.modules.mission.exceptions.ConsultantNotFoundException;
import com.shegami.hr_saas.modules.mission.exceptions.ProjectNotFoundException;
import com.shegami.hr_saas.modules.mission.mapper.ProjectMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.mission.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final TenantService tenantService;
    private final ConsultantRepository consultantRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.info("Creating project '{}' for tenant '{}'", request.getName(), tenantId);

        Tenant tenant = tenantService.getTenant(tenantId);

        Client client = clientRepository.findByClientIdAndTenantTenantId(request.getClientId(), tenantId)
                .orElseThrow(() -> {
                    log.info("Client '{}' not found during project creation", request.getClientId());
                    return new ClientNotFoundException("Client not found: " + request.getClientId());
                });

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setPriority(request.getPriority());
        project.setTags(request.getTags());
        project.setProjectStatus(ProjectStatus.IN_PROGRESS);
        project.setTenant(tenant);
        project.setClient(client);

        Project saved = projectRepository.save(project);
        log.info("Project created successfully with id '{}' for tenant '{}'", saved.getProjectId(), tenantId);

        return projectMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ProjectDto updateProject(String projectId, UpdateProjectRequest request) {
        log.info("Updating project '{}'", projectId);

        Project project = findByIdAndTenant(projectId);
        Project updatedProject = projectMapper.partialUpdate(request, project);
        ProjectDto result = projectMapper.toDto(projectRepository.save(updatedProject));

        log.info("Project '{}' updated successfully", projectId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto getProjectById(String projectId) {
        log.debug("Fetching project '{}'", projectId);
        return projectMapper.toDto(findByIdAndTenant(projectId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjects(Pageable pageable) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.debug("Fetching projects for tenant '{}' — page {}, size {}",
                tenantId, pageable.getPageNumber(), pageable.getPageSize());

        Page<ProjectDto> result = projectRepository
                .findAllByTenantTenantId(tenantId, pageable)
                .map(projectMapper::toDto);

        log.debug("Found {} projects (total: {}) for tenant '{}'",
                result.getNumberOfElements(), result.getTotalElements(), tenantId);

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(String keyword, Pageable pageable) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.debug("Searching projects with keyword '{}' for tenant '{}'", keyword, tenantId);

        Page<ProjectDto> result = projectRepository
                .searchByKeyword(keyword, tenantId, pageable)
                .map(projectMapper::toDto);

        log.debug("Search returned {} results for keyword '{}' in tenant '{}'",
                result.getTotalElements(), keyword, tenantId);

        return result;
    }

    @Override
    @Transactional
    public void changeProjectStatus(String projectId, ProjectStatus status) {
        log.info("Changing status of project '{}' to '{}'", projectId, status);

        Project project = findByIdAndTenant(projectId);
        project.setProjectStatus(status);
        projectRepository.save(project);

        log.info("Project '{}' status changed to '{}' successfully", projectId, status);
    }

    @Override
    @Transactional
    public void assignUserToProject(String projectId, String consultantId) {
        log.info("Assigning consultant '{}' to project '{}'", consultantId, projectId);

        Project project = findByIdAndTenant(projectId);
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> {
                    log.warn("Consultant '{}' not found during assignment to project '{}'", consultantId, projectId);
                    return new ConsultantNotFoundException("Consultant not found: " + consultantId);
                });

        project.getConsultants().add(consultant);
        projectRepository.save(project);

        log.info("Consultant '{}' assigned to project '{}' successfully", consultantId, projectId);
    }

    @Override
    @Transactional
    public void removeUserFromProject(String projectId, String consultantId) {
        log.info("Removing consultant '{}' from project '{}'", consultantId, projectId);

        Project project = findByIdAndTenant(projectId);
        project.getConsultants().removeIf(c -> c.getConsultantId().equals(consultantId));
        projectRepository.save(project);

        log.info("Consultant '{}' removed from project '{}' successfully", consultantId, projectId);
    }

    @Override
    @Transactional
    public void deleteProject(String projectId) {
        log.info("Deleting project '{}'", projectId);

        Project project = findByIdAndTenant(projectId);
        projectRepository.delete(project);

        log.info("Project '{}' deleted successfully", projectId);
    }

    private Project findByIdAndTenant(String projectId) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.debug("Looking up project '{}' for tenant '{}'", projectId, tenantId);

        return projectRepository
                .findByProjectIdAndTenantTenantId(projectId, tenantId)
                .orElseThrow(() -> {
                    log.warn("Project '{}' not found for tenant '{}'", projectId, tenantId);
                    return new ProjectNotFoundException("Project not found: " + projectId);
                });
    }
}
