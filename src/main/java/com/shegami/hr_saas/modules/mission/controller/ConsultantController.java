package com.shegami.hr_saas.modules.mission.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.shegami.hr_saas.modules.mission.dto.ConsultantDto;
import com.shegami.hr_saas.modules.mission.service.ConsultantService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/consultants")
public class ConsultantController {

    private final ConsultantService consultantService;

    @GetMapping
    public ResponseEntity<Page<ConsultantDto>> getAllConsultants(
            @PageableDefault(size = 10) Pageable pageable) {
        log.info("REST request to get a page of Consultants");
        return ResponseEntity.ok(consultantService.getAllConsultant(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultantDto> getConsultant(@PathVariable String id) {
        log.info("REST request to get Consultant : {}", id);
        return ResponseEntity.ok(consultantService.getConsultantById(id));
    }


    @GetMapping("/search")
    public ResponseEntity<ConsultantDto> getConsultantByEmail(@RequestParam String email) {
        log.info("REST request to get Consultant by email : {}", email);
        return ResponseEntity.ok(consultantService.getConsultantByEmail(email));
    }


    @PostMapping
    public ResponseEntity<ConsultantDto> createConsultant(@Valid @RequestBody ConsultantDto consultant) {
        log.info("REST request to save Consultant : {}", consultant.getEmail());
        ConsultantDto result = consultantService.saveConsultant(consultant);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ConsultantDto> updateConsultant(
            @PathVariable String id,
            @Valid @RequestBody ConsultantDto consultant) {
        log.info("REST request to update Consultant : {}", id);
        return ResponseEntity.ok(consultantService.updateConsultant(consultant));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsultant(@PathVariable String id) {
        log.info("REST request to delete Consultant : {}", id);
        consultantService.deleteConsultant(id);
        return ResponseEntity.noContent().build();
    }
}
