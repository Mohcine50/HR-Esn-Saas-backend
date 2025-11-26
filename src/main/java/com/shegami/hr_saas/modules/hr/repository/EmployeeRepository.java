package com.shegami.hr_saas.modules.hr.repository;

import com.shegami.hr_saas.modules.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
}
