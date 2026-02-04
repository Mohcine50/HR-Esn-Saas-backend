package com.shegami.hr_saas.modules.hr.service.implementations;

import com.shegami.hr_saas.config.domain.context.TenantContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.hr.dto.ClientDto;
import com.shegami.hr_saas.modules.hr.entity.Client;
import com.shegami.hr_saas.modules.hr.mapper.ClientMapper;
import com.shegami.hr_saas.modules.hr.repository.ClientRepository;
import com.shegami.hr_saas.modules.hr.service.ClientService;
import com.shegami.hr_saas.shared.exception.AlreadyExistsException;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final TenantService tenantService;


    @Override
    @Transactional(readOnly = true)
    public ClientDto getClientById(String id) {
        log.info("Fetching client with id: {}", id);
        return clientRepository.findById(id)
                .map(clientMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Client getClientByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with email: " + email));
    }

    @Override
    @Transactional
    public Client saveClient(Client client) {
        log.info("Saving client entity for email: {}", client.getEmail());
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public Client updateClient(Client client) {
        // Ensure the client exists before updating
        if (!clientRepository.existsById(client.getClientId())) {
            throw new ResourceNotFoundException("Cannot update. Client not found.");
        }
        return clientRepository.save(client);
    }

    @Override
    @Transactional
    public void deleteEmployee(String id) {
        log.warn("Deleting client with id: {}", id);
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ClientDto addNewClient(ClientDto clientDto) {
        log.info("Adding new client: {}", clientDto.getFullName());

        if (clientRepository.findByEmail(clientDto.getEmail()).isPresent()) {
            throw new AlreadyExistsException("Client with this email already exists");
        }


        // Get Tenant from db
        String tenantId = TenantContextHolder.getCurrentTenant();
        Tenant tenant = tenantService.getTenant(tenantId);

        Client client = clientMapper.toEntity(clientDto);
        client.setTenant(tenant);

        Client savedClient = clientRepository.save(client);

        return clientMapper.toDto(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientDto> getAllClients(Pageable pageable) {
        log.info("Fetching paged clients: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        String tenantId = TenantContextHolder.getCurrentTenant();
        return clientRepository.findByTenantId(pageable, tenantId)
                .map(clientMapper::toDto);
    }
}