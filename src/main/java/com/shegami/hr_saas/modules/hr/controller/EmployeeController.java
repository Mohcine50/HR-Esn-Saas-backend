package com.shegami.hr_saas.modules.hr.controller;


import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.dto.InvitationRequestDto;
import com.shegami.hr_saas.modules.hr.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAll(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }


    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }


    @PostMapping("/invite")
    public ResponseEntity<EmployeeDto> invite(@Valid @RequestBody InvitationRequestDto inviteDto) {
        EmployeeDto created = employeeService.addNewEmployee(inviteDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/stats/count-by-contract")
    public ResponseEntity<List<EmployeesCountByContract>> getCountByContract() {
        return ResponseEntity.ok(employeeService.countEmployeesByContract());
    }
}
