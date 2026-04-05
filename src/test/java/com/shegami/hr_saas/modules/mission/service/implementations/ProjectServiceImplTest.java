package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.dto.CreateProjectRequest;
import com.shegami.hr_saas.modules.mission.dto.ProjectDto;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.mapper.ProjectMapper;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import com.shegami.hr_saas.shared.model.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private TenantService tenantService;
    @Mock
    private ConsultantRepository consultantRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private final String TENANT_ID = "test-tenant";
    private final String USER_ID = "user-123";
    private final String PROJECT_ID = "proj-123";

    @BeforeEach
    void setUp() {
        UserContext userContext = new UserContext(USER_ID, TENANT_ID, "test@test.com", "token");
        UserContextHolder.setCurrentUserContext(userContext);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearCurrentUserContext();
    }

    @Test
    @DisplayName("Create Project - Should save and return DTO")
    void testCreateProject() {
        // Arrange
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setClientId("client-1");
        request.setPriority(Priority.HIGH);

        Tenant tenant = new Tenant();
        when(tenantService.getTenant(TENANT_ID)).thenReturn(tenant);

        Client client = new Client();
        when(clientRepository.findByClientIdAndTenantTenantId("client-1", TENANT_ID)).thenReturn(Optional.of(client));

        Project savedProject = new Project();
        savedProject.setProjectId(PROJECT_ID);
        when(projectRepository.save(any())).thenReturn(savedProject);

        ProjectDto dto = mock(ProjectDto.class);
        when(projectMapper.toDto(savedProject)).thenReturn(dto);

        // Act
        ProjectDto result = projectService.createProject(request);

        // Assert
        assertNotNull(result);
        verify(projectRepository).save(any());
        verify(clientRepository).findByClientIdAndTenantTenantId("client-1", TENANT_ID);
    }

    @Test
    @DisplayName("Assign User to Project - Should add consultant and notify")
    void testAssignUserToProject() {
        // Arrange
        Project project = new Project();
        project.setProjectId(PROJECT_ID);
        project.setName("Project Alpha");
        project.setPriority(Priority.MEDIUM);
        project.setConsultants(new HashSet<>());

        Consultant consultant = new Consultant();
        consultant.setConsultantId("cons-1");
        User user = new User();
        user.setUserId("user-cons-1");
        consultant.setUser(user);

        when(projectRepository.findByProjectIdAndTenantTenantId(PROJECT_ID, TENANT_ID))
                .thenReturn(Optional.of(project));
        when(consultantRepository.findById("cons-1")).thenReturn(Optional.of(consultant));

        // Act
        projectService.assignUserToProject(PROJECT_ID, "cons-1");

        // Assert
        assertTrue(project.getConsultants().contains(consultant));
        verify(projectRepository).save(project);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    @DisplayName("Get Project By ID - Should return DTO when found")
    void testGetProjectById_Found() {
        // Arrange
        Project project = new Project();
        project.setProjectId(PROJECT_ID);

        when(projectRepository.findByProjectIdAndTenantTenantId(PROJECT_ID, TENANT_ID))
                .thenReturn(Optional.of(project));
        ProjectDto dto = mock(ProjectDto.class);
        when(projectMapper.toDto(project)).thenReturn(dto);

        // Act
        ProjectDto result = projectService.getProjectById(PROJECT_ID);

        // Assert
        assertNotNull(result);
        verify(projectRepository).findByProjectIdAndTenantTenantId(PROJECT_ID, TENANT_ID);
    }
}
