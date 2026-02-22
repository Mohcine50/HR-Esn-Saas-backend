package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Project;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.repository.Searchable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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


    Page<Project> findAllByTenantTenantId(String tenantId, Pageable pageable);

    Optional<Project> findByProjectIdAndTenantTenantId(String projectId, String tenantId);

    @Query("""
    SELECT p FROM Project p
    WHERE p.tenant.tenantId = :tenantId
      AND (:keyword IS NULL
           OR LOWER(p.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    ORDER BY p.name ASC
""")
    Page<Project> searchByKeyword(
            @Param("keyword")  String keyword,
            @Param("tenantId") String tenantId,
            Pageable pageable
    );
}
