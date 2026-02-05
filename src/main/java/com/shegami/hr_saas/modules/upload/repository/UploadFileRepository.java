package com.shegami.hr_saas.modules.upload.repository;

import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadFileRepository extends JpaRepository<UploadFile, String> {
}