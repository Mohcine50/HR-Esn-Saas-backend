package com.shegami.hr_saas.modules.upload.service.implementations;

import com.shegami.hr_saas.config.domain.context.TenantContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.upload.dto.UploadRequest;
import com.shegami.hr_saas.modules.upload.dto.UploadResponse;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.modules.upload.mapper.FileStatus;
import com.shegami.hr_saas.modules.upload.repository.UploadFileRepository;
import com.shegami.hr_saas.modules.upload.service.UploadService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final TenantService tenantService;
    private final UploadFileRepository uploadFileRepository;
    private final S3Presigner s3Signer;
    private final UserService userService;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Transactional
    @Override
    public UploadResponse initiateUpload(UploadRequest uploadRequest) {
        String tenantId = TenantContextHolder.getCurrentTenant();
        Tenant tenant = tenantService.getTenant(tenantId);

        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findUserByEmail(userEmail).orElseThrow(
                ()-> new UserNotFoundException("User not found with email: " + userEmail));

        String s3Key = String.format("%s/%s/%s-%s",
                tenant.getTenantId(),
                uploadRequest.getFileType(),
                UUID.randomUUID(),
                uploadRequest.getFileName());

        // 1. Define the object we want to "permit" an upload for
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .contentType(uploadRequest.getContentType())
                .key(s3Key).build();

        // 2. Build the Pre-sign Request
        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.of(15, ChronoUnit.MINUTES))
                .putObjectRequest(putObjectRequest)
                .build();

        // 3. Generate the URL
        String uploadLink = s3Signer.presignPutObject(putObjectPresignRequest).url().toString();

        // 4. Save metadata (PENDING)

        UploadFile file = new UploadFile();
        file.setFileName(uploadRequest.getFileName());
        file.setFileType(uploadRequest.getFileType());
        file.setS3Key(s3Key);
        file.setStatus(FileStatus.PENDING);
        file.setUploader(user);
        file.setTenant(tenant);


        return new UploadResponse(file.getFileId(), uploadLink);
    }

    @Transactional
    @Override
    public void completeUpload(String documentId) {
        UploadFile file = uploadFileRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        // Check if the file exist on s3
        var ob = doesObjectExist(file.getS3Key());
        if (!ob) throw new ResourceNotFoundException("File not found");


        file.setStatus(FileStatus.AVAILABLE);
        uploadFileRepository.save(file);
    }

    @Transactional
    @Override
    public String generateDownloadUrl(String fileId) {

        UploadFile file = uploadFileRepository.findById(fileId).orElseThrow(
                () -> new ResourceNotFoundException("File not found")
        );

        GetObjectRequest getObjectRequest = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(file.getS3Key())
                .build();

        GetObjectPresignRequest request = GetObjectPresignRequest
                .builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.of(10, ChronoUnit.MINUTES))
                .build();

        return s3Signer.presignGetObject(request).url().toString();

    }


    public boolean doesObjectExist(String objectKey) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        try {
            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            System.err.println("Error checking object existence: " + e.awsErrorDetails().errorMessage());
            throw e;
        }
    }
}
