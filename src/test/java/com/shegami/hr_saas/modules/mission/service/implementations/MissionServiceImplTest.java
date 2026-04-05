package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.mission.dto.MissionDto;
import com.shegami.hr_saas.modules.mission.dto.NewMissionRequest;
import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.entity.Mission;
import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.modules.mission.enums.MissionStatus;
import com.shegami.hr_saas.modules.mission.enums.Priority;
import com.shegami.hr_saas.modules.mission.mapper.MissionMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.MissionRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.modules.mission.service.ClientService;
import com.shegami.hr_saas.modules.mission.service.ConsultantService;
import com.shegami.hr_saas.modules.mission.service.LabelsService;
import com.shegami.hr_saas.modules.mission.service.MissionActivityService;
import com.shegami.hr_saas.modules.upload.service.UploadService;
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MissionServiceImplTest {

    @Mock
    private MissionRepository missionRepository;
    @Mock
    private MissionMapper missionMapper;
    @Mock
    private ConsultantRepository consultantRepository;
    @Mock
    private TenantService tenantService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClientService clientService;
    @Mock
    private ConsultantService consultantService;
    @Mock
    private UploadService uploadService;
    @Mock
    private LabelsService labelsService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private MissionActivityService activityService;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MissionServiceImpl missionService;

    private final String TENANT_ID = "test-tenant";
    private final String USER_ID = "user-123";
    private final String MISSION_ID = "miss-123";

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
    @DisplayName("Get Mission By ID - Should return MissionDto when found")
    void testGetMissionById_Found() {
        // Arrange
        Mission mission = new Mission();
        mission.setMissionId(MISSION_ID);
        MissionDto missionDto = mock(MissionDto.class);
        when(missionDto.getMissionId()).thenReturn(MISSION_ID);

        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));
        when(missionMapper.toDto(mission)).thenReturn(missionDto);

        // Act
        MissionDto result = missionService.getMissionById(MISSION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(MISSION_ID, result.getMissionId());
        verify(missionRepository).findById(MISSION_ID);
    }

    @Test
    @DisplayName("Get Mission By ID - Should throw exception when not found")
    void testGetMissionById_NotFound() {
        // Arrange
        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> missionService.getMissionById(MISSION_ID));
    }

    @Test
    @DisplayName("Create Mission - Should save and return new mission")
    void testCreateMission() {
        // Arrange
        NewMissionRequest request = new NewMissionRequest(
                "New Mission", "Description", "client-1", "proj-1",
                new HashSet<>(Collections.singletonList("clt-1")),
                Priority.HIGH, MissionStatus.ACTIVE,
                Collections.emptySet(), Collections.emptySet());

        Tenant tenant = new Tenant();
        when(tenantService.getTenant(TENANT_ID)).thenReturn(tenant);

        User user = new User();
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        Employee manager = new Employee();
        user.setEmployee(manager);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));

        Project project = new Project();
        project.setProjectId("proj-1");
        when(projectRepository.findById("proj-1")).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(project);

        Consultant consultant = new Consultant();
        consultant.setFirstName("John");
        consultant.setLastName("Doe");
        consultant.setProjects(new HashSet<>()); // Initialize to avoid NPE
        User consultantUser = new User();
        consultantUser.setUserId("cons-user-1");
        consultant.setUser(consultantUser);
        when(consultantService.getAllConsultants(any()))
                .thenReturn(new HashSet<>(Collections.singletonList(consultant)));

        Mission savedMission = new Mission();
        savedMission.setMissionId(MISSION_ID);
        savedMission.setTitle("New Mission");
        savedMission.setPriority(Priority.HIGH);
        when(missionRepository.save(any())).thenReturn(savedMission);

        MissionDto resultDto = mock(MissionDto.class);
        when(resultDto.getMissionId()).thenReturn(MISSION_ID);
        when(missionMapper.toDto(savedMission)).thenReturn(resultDto);

        // Act
        MissionDto response = missionService.createMission(request);

        // Assert
        assertNotNull(response);
        assertEquals(MISSION_ID, response.getMissionId());
        verify(missionRepository).save(any());
    }

    @Test
    @DisplayName("Update Mission - Should detect and log status change")
    void testUpdateMission_StatusChange() {
        // Arrange
        Mission existingMission = new Mission();
        existingMission.setMissionId(MISSION_ID);
        existingMission.setStatus(MissionStatus.ACTIVE);
        existingMission.setConsultants(new HashSet<>());

        Consultant actor = new Consultant();
        actor.setFirstName("Actor");
        actor.setLastName("Name");
        actor.setProjects(new HashSet<>());
        when(consultantRepository.findByUserUserId(USER_ID)).thenReturn(Optional.of(actor));

        MissionDto updateDto = mock(MissionDto.class);
        when(updateDto.getStatus()).thenReturn(MissionStatus.COMPLETED);

        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(existingMission));
        when(missionRepository.save(any())).thenReturn(existingMission);

        MissionDto resultDto = mock(MissionDto.class);
        when(resultDto.getStatus()).thenReturn(MissionStatus.COMPLETED);
        when(missionMapper.toDto(any())).thenReturn(resultDto);

        // Act
        MissionDto response = missionService.updateMission(updateDto, MISSION_ID);

        // Assert
        assertNotNull(response);
        assertEquals(MissionStatus.COMPLETED, response.getStatus());
        verify(activityService).log(eq(existingMission),
                eq(com.shegami.hr_saas.modules.mission.enums.ActivityType.STATUS_CHANGED), any(), any(), any(), any(),
                any());
    }

    @Test
    @DisplayName("Delete Mission - Should log activity and delete")
    void testDeleteMission() {
        // Arrange
        Mission mission = new Mission();
        mission.setMissionId(MISSION_ID);
        mission.setTitle("Delete Me");

        Consultant actor = new Consultant();
        actor.setFirstName("Actor");
        actor.setLastName("Name");
        actor.setProjects(new HashSet<>());
        when(consultantRepository.findByUserUserId(USER_ID)).thenReturn(Optional.of(actor));

        when(missionRepository.findById(MISSION_ID)).thenReturn(Optional.of(mission));

        // Act
        missionService.deleteMission(MISSION_ID);

        // Assert
        verify(activityService).log(eq(mission),
                eq(com.shegami.hr_saas.modules.mission.enums.ActivityType.MISSION_DELETED), any(), any(), any());
        verify(missionRepository).deleteById(MISSION_ID);
    }
}
