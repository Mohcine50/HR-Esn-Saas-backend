package com.shegami.hr_saas.modules.hr.service;

import com.shegami.hr_saas.modules.hr.dto.InviteEmployeeDto;
import com.shegami.hr_saas.modules.hr.entity.Employee;

public interface EmployeeService {

    Employee getEmployeeById(String id);
    Employee getEmployeeByEmail(String Email);

    Employee saveEmployee(Employee employee);
    Employee updateEmployee(Employee employee);
    void deleteEmployee(String id);

    String AddNewEmployee(InviteEmployeeDto employee);
}
