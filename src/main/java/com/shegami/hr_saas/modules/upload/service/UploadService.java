package com.shegami.hr_saas.modules.upload.service;

import com.shegami.hr_saas.modules.upload.dto.UploadRequest;
import com.shegami.hr_saas.modules.upload.dto.UploadResponse;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.util.Set;

public interface UploadService {
    UploadResponse initiateUpload(UploadRequest uploadRequest);
    void completeUpload(String fileId);
    String generateDownloadUrl(String fileId);
    UploadFile getUploadFile(String fileId);
    Set<UploadFile> getUploadFiles(Set<String> fileIds);

    String resolveUrl(UploadFile file);
}
