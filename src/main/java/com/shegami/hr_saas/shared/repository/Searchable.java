package com.shegami.hr_saas.shared.repository;

import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface Searchable {
    Page<DropdownOptionDTO> searchForDropdown(String search, String tenantId, Pageable pageable);
}