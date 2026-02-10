package com.shegami.hr_saas.modules.mission.repository;

import com.shegami.hr_saas.modules.mission.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelRepository extends JpaRepository<Label, String> {
}