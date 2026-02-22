package com.shegami.hr_saas.modules.hr.repository;

import com.shegami.hr_saas.modules.hr.dto.EmployeesCountByContract;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.repository.Searchable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, String> {


    @Query("SELECT e FROM Employee e WHERE e.tenant.tenantId = :tenant and e.user.userId <> :userId")
    Page<Employee> findByTenantId(Pageable pageable, @Param("tenant") String tenant, @Param("userId") String userId);

    @Query("""
             SELECT e.contractType, count(*) AS count\s
             FROM Employee e\s
             WHERE e.tenant.tenantId = :tenant
             GROUP BY e.contractType
            \s""")
    List<EmployeesCountByContract> countEmployeeByContractType(@Param("tenant") String tenant);

}
