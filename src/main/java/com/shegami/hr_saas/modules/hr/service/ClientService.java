package com.shegami.hr_saas.modules.hr.service;

import com.shegami.hr_saas.modules.hr.dto.ClientDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.dto.InviteEmployeeDto;
import com.shegami.hr_saas.modules.hr.entity.Client;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



public interface ClientService {
    ClientDto getClientById(String id);
    Client getClientByEmail(String Email);

    Client saveClient(Client client);
    Client updateClient(Client client);
    void deleteEmployee(String id);

    ClientDto addNewClient(ClientDto clientDto);
    Page<ClientDto> getAllClients(Pageable pageable);
}
