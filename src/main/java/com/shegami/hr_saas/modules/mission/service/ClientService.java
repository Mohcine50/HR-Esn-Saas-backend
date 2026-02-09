package com.shegami.hr_saas.modules.mission.service;

import com.shegami.hr_saas.modules.mission.dto.ClientDto;
import com.shegami.hr_saas.modules.mission.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ClientService {
    ClientDto getClientById(String id);
    Client getClientByEmail(String Email);
    Client getClientByIdForMission(String id);

    Client saveClient(Client client);
    Client updateClient(Client client);
    void deleteEmployee(String id);

    ClientDto addNewClient(ClientDto clientDto);
    Page<ClientDto> getAllClients(Pageable pageable);
}
