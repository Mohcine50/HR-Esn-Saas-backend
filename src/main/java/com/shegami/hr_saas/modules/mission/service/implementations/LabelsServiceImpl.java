package com.shegami.hr_saas.modules.mission.service.implementations;

import com.shegami.hr_saas.modules.mission.entity.Label;
import com.shegami.hr_saas.modules.mission.repository.LabelRepository;
import com.shegami.hr_saas.modules.mission.service.LabelsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LabelsServiceImpl implements LabelsService {
    private final LabelRepository labelRepository;
    @Override
    public Set<Label> getAllLabels(Set<String> ids) {
        return new HashSet<>(labelRepository.findAllById(ids));
    }
}
