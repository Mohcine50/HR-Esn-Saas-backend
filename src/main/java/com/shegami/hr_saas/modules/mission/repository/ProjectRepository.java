package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.repository.Searchable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, String>, Searchable {
    @Query(
            value = """
        SELECT p.project_id AS id,
               p.name       AS name
        FROM projects p
        WHERE p.tenant_id = :tenantId
          AND (
            :search IS NULL
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
          )
        ORDER BY p.name ASC
    """,
            countQuery = """
        SELECT COUNT(p.project_id)
        FROM projects p
        WHERE p.tenant_id = :tenantId
          AND (
            :search IS NULL
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
          )
    """,
            nativeQuery = true
    )
    Page<DropdownOptionDTO> searchForDropdown(
            @Param("search")   String search,
            @Param("tenantId") String tenantId,
            Pageable pageable
    );
}
