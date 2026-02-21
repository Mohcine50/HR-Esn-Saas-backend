package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Client;
import com.shegami.hr_saas.modules.mission.entity.Consultant;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.repository.Searchable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConsultantRepository extends JpaRepository<Consultant, String>, Searchable {
    @Query("SELECT c FROM Consultant c WHERE c.tenant.tenantId = :tenant")
    Page<Consultant> findByTenantId(Pageable pageable, @Param("tenant") String tenant);
    Optional<Consultant> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT new com.shegami.hr_saas.shared.dto.DropdownOptionDTO(c.consultantId, CONCAT(c.firstName, ' ' , c.lastName) )
        FROM Consultant c
        WHERE c.tenant.tenantId = :tenantId
          AND (
                    :search IS NULL
                    OR LOWER(c.user.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(c.user.lastName)  LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(CONCAT(c.user.firstName, ' ', c.user.lastName)) LIKE LOWER(CONCAT('%', :search, '%'))
                  )
                ORDER BY c.user.firstName ASC
    """)
    Page<DropdownOptionDTO> searchForDropdown(
            @Param("search")   String search,
            @Param("tenantId") String tenantId,
            Pageable pageable
    );
}