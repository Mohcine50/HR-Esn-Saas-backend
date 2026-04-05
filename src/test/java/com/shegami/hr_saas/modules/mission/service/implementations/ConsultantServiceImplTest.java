package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.entity.UserRole;
import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import com.shegami.hr_saas.modules.auth.enums.UserRoles;
import com.shegami.hr_saas.modules.auth.mapper.UserMapper;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.repository.UserSettingsRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserRoleService;
import com.shegami.hr_saas.modules.hr.repository.InvitationRepository;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.modules.mission.mapper.ConsultantMapper;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.notifications.rabbitmq.publisher.EventPublisher;
import com.shegami.hr_saas.shared.exception.AlreadyExistsException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultantServiceImplTest {

    @Mock
    private ConsultantRepository consultantRepository;
    @Mock
    private ConsultantMapper consultantMapper;
    @Mock
    private TenantService tenantService;
    @Mock
    private UserRoleService userRoleService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ConsultantServiceImpl consultantService;

    private final String TENANT_ID = "test-tenant";
    private final String USER_ID = "user-123";
    private final String CONSULTANT_ID = "clt-123";

    @BeforeEach
    void setUp() {
        UserContext userContext = new UserContext(USER_ID, TENANT_ID, "test@test.com", "token");
        UserContextHolder.setCurrentUserContext(userContext);
        ReflectionTestUtils.setField(consultantService, "invitationExpiryDays", 7);
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clearCurrentUserContext();
    }

    @Test
    @DisplayName("Get Consultant By ID - Should return DTO when found")
    void testGetConsultantById_Found() {
        // Arrange
        Consultant consultant = new Consultant();
        consultant.setConsultantId(CONSULTANT_ID);
        ConsultantDto consultantDto = mock(ConsultantDto.class);
        when(consultantDto.getConsultantId()).thenReturn(CONSULTANT_ID);

        when(consultantRepository.findById(CONSULTANT_ID)).thenReturn(Optional.of(consultant));
        when(consultantMapper.toDto(consultant)).thenReturn(consultantDto);

        // Act
        ConsultantDto result = consultantService.getConsultantById(CONSULTANT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(CONSULTANT_ID, result.getConsultantId());
        verify(consultantRepository).findById(CONSULTANT_ID);
    }

    @Test
    @DisplayName("Get Consultant By ID - Should throw exception when not found")
    void testGetConsultantById_NotFound() {
        // Arrange
        when(consultantRepository.findById(CONSULTANT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> consultantService.getConsultantById(CONSULTANT_ID));
    }

    @Test
    @DisplayName("Save Consultant - Should throw exception if email already exists")
    void testSaveConsultant_EmailExists() {
        // Arrange
        ConsultantDto dto = mock(ConsultantDto.class);
        when(dto.getEmail()).thenReturn("existing@test.com");
        when(consultantRepository.existsByEmail("existing@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> consultantService.saveConsultant(dto));
    }

    @Test
    @DisplayName("Save Consultant - Should create everything correctly")
    void testSaveConsultant_Success() {
        // Arrange
        ConsultantDto dto = mock(ConsultantDto.class);
        when(dto.getEmail()).thenReturn("new@test.com");
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn("Doe");

        when(consultantRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(invitationRepository.findByInviteeEmailAndTenantTenantIdAndStatus(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        Tenant tenant = new Tenant();
        tenant.setName("Test Tenant");
        when(tenantService.getTenant(TENANT_ID)).thenReturn(tenant);

        User inviter = new User();
        inviter.setFirstName("Inviter");
        when(userRepository.findUsersByUserId(USER_ID)).thenReturn(Optional.of(inviter));

        UserRole role = new UserRole();
        when(userRoleService.getUserRoleByName(UserRoles.CONSULTANT)).thenReturn(role);
        when(userSettingsRepository.save(any())).thenReturn(new UserSettings());
        when(passwordEncoder.encode(any())).thenReturn("hashed-pwd");

        User savedUser = new User();
        savedUser.setUserId("new-user-id");
        when(userRepository.save(any())).thenReturn(savedUser);

        Consultant consultantEntity = new Consultant();
        consultantEntity.setEmail("new@test.com");
        when(consultantMapper.toEntity(dto)).thenReturn(consultantEntity);
        when(consultantRepository.save(any())).thenReturn(consultantEntity);
        when(consultantMapper.toDto(any())).thenReturn(dto);

        // Act
        ConsultantDto result = consultantService.saveConsultant(dto);

        // Assert
        assertNotNull(result);
        verify(userRepository).save(any());
        verify(invitationRepository).save(any());
        verify(eventPublisher).publishInvitationEmail(any());
        verify(consultantRepository).save(any());
    }

    @Test
    @DisplayName("Update Consultant - Should save and return updated DTO")
    void testUpdateConsultant_Success() {
        // Arrange
        ConsultantDto dto = mock(ConsultantDto.class);
        when(dto.getConsultantId()).thenReturn(CONSULTANT_ID);

        when(consultantRepository.existsById(CONSULTANT_ID)).thenReturn(true);
        Consultant entity = new Consultant();
        when(consultantMapper.toEntity(dto)).thenReturn(entity);
        when(consultantRepository.save(entity)).thenReturn(entity);
        when(consultantMapper.toDto(entity)).thenReturn(dto);

        // Act
        ConsultantDto result = consultantService.updateConsultant(dto);

        // Assert
        assertNotNull(result);
        verify(consultantRepository).save(entity);
    }

    @Test
    @DisplayName("Delete Consultant - Should delete when exists")
    void testDeleteConsultant_Success() {
        // Arrange
        when(consultantRepository.existsById(CONSULTANT_ID)).thenReturn(true);

        // Act
        consultantService.deleteConsultant(CONSULTANT_ID);

        // Assert
        verify(consultantRepository).deleteById(CONSULTANT_ID);
    }

    @Test
    @DisplayName("Get All Consultants - Should return paged result")
    void testGetAllConsultant() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Consultant consultant = new Consultant();
        Page<Consultant> page = new PageImpl<>(Collections.singletonList(consultant));

        when(consultantRepository.findByTenantId(pageable, TENANT_ID)).thenReturn(page);
        when(consultantMapper.toDto(any())).thenReturn(mock(ConsultantDto.class));

        // Act
        Page<ConsultantDto> result = consultantService.getAllConsultant(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(consultantRepository).findByTenantId(pageable, TENANT_ID);
    }
}
