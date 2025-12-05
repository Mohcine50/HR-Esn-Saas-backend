package com.shegami.hr_saas.modules.hr.repository;

import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, String> {


    @Query("SELECT e FROM Employee e WHERE e.tenant.tenantId = :tenant")
    Page<Employee> findByTenantId(Pageable pageable, @Param("tenant") String tenant);

}
