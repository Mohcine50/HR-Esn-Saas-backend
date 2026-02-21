package com.shegami.hr_saas.shared.controller;

import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.dto.DropdownResponse;
import com.shegami.hr_saas.shared.dto.DropdownSearchRequest;
import com.shegami.hr_saas.shared.service.DropdownService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("api/dropdown")
public class DropdownController {
    private final DropdownService dropdownService;

    @GetMapping("/projects")
    public ResponseEntity<DropdownResponse<DropdownOptionDTO>> searchProjects(
            @Valid DropdownSearchRequest request
    ) {
        return ResponseEntity.ok(
                dropdownService.searchProjects(request.getSearch(), request.getLimit())
        );
    }

    @GetMapping("/consultants")
    public ResponseEntity<DropdownResponse<DropdownOptionDTO>> searchConsultants(
            @Valid DropdownSearchRequest request
    ) {
        return ResponseEntity.ok(
                dropdownService.searchConsultants(request.getSearch(), request.getLimit())
        );
    }

    @GetMapping("/labels")
    public ResponseEntity<DropdownResponse<DropdownOptionDTO>> searchLabels(
            @Valid DropdownSearchRequest request
    ) {
        return ResponseEntity.ok(
                dropdownService.searchLabels(request.getSearch(), request.getLimit())
        );
    }
}
