package com.shegami.hr_saas.modules.upload.service;

import com.shegami.hr_saas.modules.upload.dto.UploadRequest;
import com.shegami.hr_saas.modules.upload.dto.UploadResponse;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.util.Set;

import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.upload.mapper.FileType;

public interface UploadService {
    UploadResponse initiateUpload(UploadRequest uploadRequest);

    void completeUpload(String fileId);

    String generateDownloadUrl(String fileId);

    UploadFile getUploadFile(String fileId);

    Set<UploadFile> getUploadFiles(Set<String> fileIds);

    String resolveUrl(UploadFile file);

    UploadFile uploadInternalFile(byte[] content, String fileName, FileType fileType, String contentType, Tenant tenant,
            User uploader);
}
