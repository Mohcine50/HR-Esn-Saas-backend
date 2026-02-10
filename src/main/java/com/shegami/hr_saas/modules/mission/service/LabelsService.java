package com.shegami.hr_saas.modules.mission.service;


import com.shegami.hr_saas.modules.mission.entity.Label;

import java.util.Set;

public interface LabelsService {
    Set<Label> getAllLabels(Set<String> ids);

}
