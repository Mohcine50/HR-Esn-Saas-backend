package com.shegami.hr_saas.modules.auth.repository;

import com.shegami.hr_saas.modules.auth.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
}