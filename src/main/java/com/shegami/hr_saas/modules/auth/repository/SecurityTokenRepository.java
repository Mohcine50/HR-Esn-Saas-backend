package com.shegami.hr_saas.modules.auth.repository;

import com.shegami.hr_saas.modules.auth.entity.SecurityToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityTokenRepository extends JpaRepository<SecurityToken, String> {
  Optional<SecurityToken> findByToken(String token);

  void deleteByToken(String token);

  void deleteByUserUserId(String userId);
}