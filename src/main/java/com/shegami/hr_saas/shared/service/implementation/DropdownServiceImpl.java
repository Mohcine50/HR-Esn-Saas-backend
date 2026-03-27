package com.shegami.hr_saas.shared.service.implementation;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.mission.repository.ClientRepository;
import com.shegami.hr_saas.modules.mission.repository.ConsultantRepository;
import com.shegami.hr_saas.modules.mission.repository.LabelRepository;
import com.shegami.hr_saas.modules.mission.repository.ProjectRepository;
import com.shegami.hr_saas.shared.dto.DropdownOptionDTO;
import com.shegami.hr_saas.shared.dto.DropdownResponse;
import com.shegami.hr_saas.shared.repository.Searchable;
import com.shegami.hr_saas.shared.service.DropdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DropdownServiceImpl implements DropdownService {
    private final ProjectRepository projectRepository;
    private final ConsultantRepository consultantRepository;
    private final LabelRepository labelRepository;
    private final ClientRepository clientRepository;

    public DropdownResponse<DropdownOptionDTO> searchProjects(String search, int limit) {
        return query(projectRepository, search, limit);
    }

    @Override
    public DropdownResponse<DropdownOptionDTO> searchClients(String search, int limit) {
        return query(clientRepository, search, limit);
    }

    public DropdownResponse<DropdownOptionDTO> searchConsultants(String search, int limit) {
        return query(consultantRepository, search, limit);
    }

    public DropdownResponse<DropdownOptionDTO> searchLabels(String search, int limit) {
        return query(labelRepository, search, limit);
    }

    private DropdownResponse<DropdownOptionDTO> query(Searchable repository, String search, int limit) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        Page<DropdownOptionDTO> page = repository.searchForDropdown(
                nullIfBlank(search),
                tenantId,
                PageRequest.of(0, limit));
        return new DropdownResponse<>(page.getContent(), page.getTotalElements());
    }

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

}
