package com.shegami.hr_saas.modules.upload.service.implementations;

import com.shegami.hr_saas.config.domain.context.UserContextHolder;
import com.shegami.hr_saas.modules.auth.entity.Tenant;
import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.auth.exception.UserNotFoundException;
import com.shegami.hr_saas.modules.auth.repository.TenantRepository;
import com.shegami.hr_saas.modules.auth.repository.UserRepository;
import com.shegami.hr_saas.modules.auth.service.TenantService;
import com.shegami.hr_saas.modules.auth.service.UserService;
import com.shegami.hr_saas.modules.upload.dto.UploadRequest;
import com.shegami.hr_saas.modules.upload.dto.UploadResponse;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.modules.upload.exceptions.StorageUploadException;
import com.shegami.hr_saas.modules.upload.mapper.FileStatus;
import com.shegami.hr_saas.modules.upload.mapper.FileType;
import com.shegami.hr_saas.modules.upload.repository.UploadFileRepository;
import com.shegami.hr_saas.modules.upload.service.UploadService;
import com.shegami.hr_saas.shared.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final TenantService          tenantService;
    private final UploadFileRepository   uploadFileRepository;
    private final S3Presigner            s3Signer;
    private final UserService            userService;
    private final S3Client               s3Client;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;


    @Value("${aws.s3.endpoint}")
    private String endpoint;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private static final Set<FileType> PUBLIC_TYPES = Set.of(
            FileType.PROFILE,
            FileType.COMPANY_LOGO
    );
    @Transactional
    @Override
    public UploadResponse initiateUpload(UploadRequest uploadRequest) {
        String tenantId  = UserContextHolder.getCurrentUserContext().tenantId();
        Tenant tenant    = tenantService.getTenant(tenantId);

        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userEmail));

        boolean isPublic = PUBLIC_TYPES.contains(uploadRequest.getFileType());
        String  prefix   = isPublic ? "public" : "private";

        String s3Key = String.format("%s/%s/%s/%s-%s",
                prefix,
                tenantId,
                uploadRequest.getFileType().name().toLowerCase(),
                UUID.randomUUID(),
                uploadRequest.getFileName());

        log.info("[Upload] Initiating upload | tenantId={} fileType={} isPublic={} key={}",
                tenantId, uploadRequest.getFileType(), isPublic, s3Key);

        // ── Presigned PUT ─────────────────────────────────────────────────────
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(uploadRequest.getContentType())
                .acl(isPublic ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.PRIVATE)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.of(15, ChronoUnit.MINUTES))
                .putObjectRequest(putObjectRequest)
                .build();

        String uploadUrl = s3Signer.presignPutObject(presignRequest).url().toString();

        // ── Public URL — stored permanently, no signing needed later ──────────
        // MinIO format: http://localhost:9000/bucket/key
        // AWS format:   https://bucket.s3.region.amazonaws.com/key
        String publicUrl = isPublic
                ? "%s/%s/%s".formatted(endpoint, bucketName, s3Key)
                : null;

        // ── Persist metadata ──────────────────────────────────────────────────
        UploadFile file = new UploadFile();
        file.setFileName(uploadRequest.getFileName());
        file.setFileType(uploadRequest.getFileType());
        file.setContentType(uploadRequest.getContentType());
        file.setS3Key(s3Key);
        file.setPublic(isPublic);
        file.setPublicUrl(publicUrl); // ← stored permanently
        file.setStatus(FileStatus.PENDING);
        file.setUploader(user);
        file.setTenant(tenant);

        UploadFile saved = uploadFileRepository.save(file);

        switch (saved.getFileType()) {
            case PROFILE -> {
                user.setImageUrl(saved);
                userRepository.save(user);
            }
            case COMPANY_LOGO -> {
                tenant.setImageUrl(saved);
                tenantRepository.save(tenant);
            }
        }

        log.info("[Upload] Upload initiated | fileId={} key={} isPublic={}",
                saved.getFileId(), s3Key, isPublic);

        return new UploadResponse(saved.getFileId(), uploadUrl);
    }


    @Transactional
    @Override
    public void completeUpload(String fileId) {
        UploadFile file = uploadFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        log.info("[Upload] Completing upload | fileId={} key={}", fileId, file.getS3Key());

        if (!doesObjectExist(file.getS3Key())) {
            log.warn("[Upload] Object not found on S3 | fileId={} key={}", fileId, file.getS3Key());
            throw new StorageUploadException("File not found on storage: " + file.getS3Key());
        }

        file.setStatus(FileStatus.AVAILABLE);
        uploadFileRepository.save(file);

        log.info("[Upload] Upload completed | fileId={} isPublic={}", fileId, file.isPublic());
    }

    @Transactional(readOnly = true)
    @Override
    public String generateDownloadUrl(String fileId) {
        UploadFile file = uploadFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));

        // Public files — return stored permanent URL, never hits S3 again
        if (file.isPublic() && file.getPublicUrl() != null) {
            log.debug("[Upload] Returning public URL | fileId={}", fileId);
            return file.getPublicUrl();
        }

        // Private files — generate signed URL
        log.debug("[Upload] Generating signed URL | fileId={} key={}", fileId, file.getS3Key());

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(r -> r.bucket(bucketName).key(file.getS3Key()))
                .signatureDuration(Duration.of(10, ChronoUnit.MINUTES))
                .build();

        return s3Signer.presignGetObject(presignRequest).url().toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public UploadFile getUploadFile(String fileId) {
        return uploadFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
    }

    @Override
    public Set<UploadFile> getUploadFiles(Set<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(uploadFileRepository.findAllById(fileIds));
    }
    @Override
    @Transactional
    public String resolveUrl(UploadFile file) {
        if (file == null) return null;
        if (file.isPublic() && file.getPublicUrl() != null) {
            return file.getPublicUrl();
        }
        return generateDownloadUrl(file.getFileId());
    }

    public boolean doesObjectExist(String s3Key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.error("[Upload] S3 error checking object | key={} error={}", s3Key, e.getMessage());
            throw e;
        }
    }
}