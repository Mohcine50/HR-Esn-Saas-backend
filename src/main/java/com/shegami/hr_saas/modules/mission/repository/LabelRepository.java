package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LabelRepository extends JpaRepository<Label, String> {
    @Query("SELECT l FROM Label l WHERE l.tenant.tenantId = :tenant")
    Page<Label> findAllByTenantId(Pageable pageable, @Param("tenant") String tenant);

    @Query("""
    SELECT l FROM Label l\s
    WHERE l.tenant.tenantId = :tenantId\s
    AND (LOWER(l.labelName) LIKE LOWER(CONCAT('%', :query, '%'))\s
    OR LOWER(l.labelDescription) LIKE LOWER(CONCAT('%', :query, '%')))
    """)
    List<Label> search(@Param("tenantId") String tenantId, @Param("query") String query);
    Optional<Label> findByLabelIdAndTenantTenantId(String id, String tenantId);
    Optional<Label> findByLabelNameAndTenantTenantId(String labelName, String tenantId);
    boolean existsByLabelNameAndTenantTenantId(String name, String tenantId);
    boolean existsByLabelIdAndTenantTenantId(String id, String tenantId);
    void deleteByLabelIdAndTenantTenantId(String id, String tenantId);

}