package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.shared.repository.Searchable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, String>, Searchable {

    @Query("SELECT c FROM Client c WHERE c.tenant.tenantId = :tenant")
    Page<Client> findByTenantId(Pageable pageable, @Param("tenant") String tenant);
    Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);

}