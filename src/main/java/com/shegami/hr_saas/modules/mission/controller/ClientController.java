package com.shegami.hr_saas.modules.mission.controller;

import com.shegami.hr_saas.modules.mission.dto.ClientDto;
import com.shegami.hr_saas.modules.mission.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clients")
@Slf4j
public
class ClientController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<Page<ClientDto>> getAll(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(clientService.getAllClients(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }


    @PostMapping("/new")
    public ResponseEntity<ClientDto> newClient(@Valid @RequestBody ClientDto clientDto) {
        // Return the created object, not the password
        ClientDto created = clientService.addNewClient(clientDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}

