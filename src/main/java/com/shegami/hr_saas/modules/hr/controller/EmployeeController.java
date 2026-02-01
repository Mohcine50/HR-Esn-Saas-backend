package com.shegami.hr_saas.modules.hr.controller;


import com.shegami.hr_saas.modules.auth.dto.InviteDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.dto.InviteEmployeeDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.modules.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("all")
    public ResponseEntity<Page<EmployeeDto>> listEmployees(
            @PageableDefault(size = 10, page = 0) Pageable pageable
    ) {
        Page<EmployeeDto> page = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(page);
    }


    @GetMapping()
    public ResponseEntity<EmployeeDto> getEmployee(
            @RequestParam("employee_id") String employeeId
    ) {
        EmployeeDto employee = employeeService.getEmployeeById(employeeId);
        return ResponseEntity.ok(employee);
    }

    @PostMapping("invite")
    public ResponseEntity<Object> inviteEmployee(@RequestBody InviteEmployeeDto inviteDto){
        String generatedPassword = employeeService.AddNewEmployee(inviteDto);
        return ResponseEntity.ok(generatedPassword);
    }


    @GetMapping("/employees-count-by-contract")
    public ResponseEntity<List<EmployeesCountByContract>> countEmployeeByContract(){
        List<EmployeesCountByContract> res =employeeService.countEmployeesByContract();
        return ResponseEntity.ok(res);
    }


}
