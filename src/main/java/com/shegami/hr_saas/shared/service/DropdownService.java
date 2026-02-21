package com.shegami.hr_saas.shared.service;

import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.dto.DropdownResponse;

public interface DropdownService {
    DropdownResponse<DropdownOptionDTO> searchProjects(String search, int limit);
    DropdownResponse<DropdownOptionDTO> searchConsultants(String search, int limit);
    DropdownResponse<DropdownOptionDTO> searchLabels(String search, int limit);
}
