package com.shegami.hr_saas.modules.hr.controller;


import com.shegami.hr_saas.modules.auth.dto.InviteDto;
import com.shegami.hr_saas.modules.hr.dto.InviteEmployeeDto;
import com.shegami.hr_saas.modules.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@Slf4j
public class EmployeeController {



    private final EmployeeService employeeService;

    @PostMapping("invite")
    public ResponseEntity<Object> inviteEmployee(@RequestBody InviteEmployeeDto inviteDto){
        String generatedPassword = employeeService.AddNewEmployee(inviteDto);
        return ResponseEntity.ok(generatedPassword);
    }



}
