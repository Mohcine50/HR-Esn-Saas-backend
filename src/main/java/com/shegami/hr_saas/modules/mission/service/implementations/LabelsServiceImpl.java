package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.mission.dto.LabelDto;
import com.shegami.hr_saas.modules.mission.mapper.LabelMapper;
import com.shegami.hr_saas.modules.mission.entity.Label;
import com.shegami.hr_saas.modules.mission.repository.LabelRepository;
import com.shegami.hr_saas.modules.mission.service.LabelsService;
import com.shegami.hr_saas.shared.exception.AlreadyExistsException;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabelsServiceImpl implements LabelsService {
    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final TenantService tenantService;

    @Override
    public Set<Label> getAllLabels(Set<String> ids) {
        return new HashSet<>(labelRepository.findAllById(ids));
    }

    @Override
    @Transactional(readOnly = true)
    public LabelDto getLabelById(String id) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return labelRepository.findByLabelIdAndTenantTenantId(id, tenantId)
                .map(labelMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public LabelDto getLabelByName(String name) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return labelRepository.findByLabelNameAndTenantTenantId(name, tenantId)
                .map(labelMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with name: " + name));
    }

    @Override
    @Transactional
    public LabelDto saveLabel(LabelDto labelDto) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.info("Saving new label '{}' for tenant {}", labelDto.getLabelName(), tenantId);

        if (labelRepository.existsByLabelNameAndTenantTenantId(labelDto.getLabelName(), tenantId)) {
            throw new AlreadyExistsException("A label with this name already exists in your workspace.");
        }

        Tenant tenant = tenantService.getTenant(tenantId);


        Label label = labelMapper.toEntity(labelDto);

        label.setTenant(tenant);
        return labelMapper.toDto(labelRepository.save(label));
    }

    @Override
    @Transactional
    public LabelDto updateLabel(LabelDto labelDto) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();

        Label existingLabel = labelRepository.findByLabelIdAndTenantTenantId(labelDto.getLabelId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));

        labelMapper.partialUpdate(labelDto, existingLabel);

        return labelMapper.toDto(labelRepository.save(existingLabel));
    }

    @Override
    @Transactional
    public void deleteLabel(String id) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.warn("Deleting label {} for tenant {}", id, tenantId);

        if (!labelRepository.existsByLabelIdAndTenantTenantId(id, tenantId)) {
            throw new ResourceNotFoundException("Label not found or access denied");
        }

        labelRepository.deleteByLabelIdAndTenantTenantId(id, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LabelDto> getAllLabels(Pageable pageable) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        return labelRepository.findAllByTenantId(pageable, tenantId)
                .map(labelMapper::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public List<LabelDto> searchLabels(String query) {
        String tenantId = UserContextHolder.getCurrentUserContext().tenantId();
        log.debug("Searching labels with query '{}' for tenant {}", query, tenantId);

        if (query == null || query.trim().isEmpty()) {

            return labelRepository.findAllByTenantId(Pageable.unpaged(), tenantId)
                    .getContent()
                    .stream()
                    .map(labelMapper::toDto)
                    .toList();
        }

        return labelRepository.search(tenantId, query.trim())
                .stream()
                .map(labelMapper::toDto)
                .toList();
    }
}
