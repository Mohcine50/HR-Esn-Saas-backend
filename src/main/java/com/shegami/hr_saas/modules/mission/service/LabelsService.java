package com.shegami.hr_saas.modules.mission.service;


import com.shegami.hr_saas.modules.mission.dto.LabelDto;
import com.shegami.hr_saas.modules.mission.entity.Label;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

public interface LabelsService {
    Set<Label> getAllLabels(Set<String> ids);
    LabelDto saveLabel(LabelDto labelDto);
    LabelDto getLabelById(String id);
    LabelDto updateLabel(LabelDto labelDto);
    void deleteLabel(String id);
    Page<LabelDto> getAllLabels(Pageable pageable);
    LabelDto getLabelByName(String name);
    List<LabelDto> searchLabels(String query);


}
