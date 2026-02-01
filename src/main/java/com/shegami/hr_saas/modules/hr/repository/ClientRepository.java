package com.shegami.hr_saas.modules.hr.repository;

import com.shegami.hr_saas.modules.hr.entity.Client;
import com.shegami.hr_saas.modules.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String> {

    @Query("SELECT c FROM Client c WHERE c.tenant.tenantId = :tenant")
    Page<Client> findByTenantId(Pageable pageable, @Param("tenant") String tenant);
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);

}