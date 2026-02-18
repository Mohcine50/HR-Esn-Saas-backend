package com.shegami.hr_saas.modules.hr.service;

import com.shegami.hr_saas.modules.hr.dto.EmployeeDto;
import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.dto.InvitationRequestDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {

    EmployeeDto getEmployeeById(String id);
    Employee getEmployeeByEmail(String Email);

    Employee saveEmployee(Employee employee);
    Employee updateEmployee(Employee employee);
    void deleteEmployee(String id);

    EmployeeDto AddNewEmployee(InvitationRequestDto employee);
    Page<EmployeeDto> getAllEmployees(Pageable pageable);


    List<EmployeesCountByContract> countEmployeesByContract();
}
