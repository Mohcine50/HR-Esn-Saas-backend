package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
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

    Optional<Client> findByClientIdAndTenantTenantId(String clientId, String tenantId);

    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(value = """
                SELECT c.client_id AS id,
                       c.full_name AS name
                FROM clients c
                WHERE c.tenant_id = :tenantId
                  AND (
                    :search IS NULL
                    OR LOWER(c.full_name) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
                ORDER BY c.full_name ASC
            """, countQuery = """
                SELECT COUNT(c.client_id)
                FROM clients c
                WHERE c.tenant_id = :tenantId
                  AND (
                    :search IS NULL
                    OR LOWER(c.full_name) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """, nativeQuery = true)
    Page<DropdownOptionDTO> searchForDropdown(
            @Param("search") String search,
            @Param("tenantId") String tenantId,
            Pageable pageable);
}