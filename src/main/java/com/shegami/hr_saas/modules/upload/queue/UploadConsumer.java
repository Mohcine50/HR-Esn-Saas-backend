package com.shegami.hr_saas.modules.upload.queue;

import com.shegami.hr_saas.config.domain.rabbitMq.RabbitMQConfig;
import com.shegami.hr_saas.modules.upload.entity.UploadFile;
import com.shegami.hr_saas.modules.upload.mapper.FileStatus;
import com.shegami.hr_saas.modules.upload.repository.UploadFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import com.shegami.hr_saas.modules.upload.dto.FileUploadMessage;

@Component
@Slf4j
@RequiredArgsConstructor
public class UploadConsumer {

    private final UploadFileRepository uploadFileRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @RabbitListener(queues = RabbitMQConfig.UPLOAD_QUEUE)
    @Transactional
    public void handleFileUpload(FileUploadMessage message) {
        String fileId = message.getFileId();
        byte[] content = message.getContent();

        log.info("[UploadQueue] Received upload task for fileId: {}", fileId);

        if (fileId == null) {
            log.warn("[UploadQueue] Received null fileId");
            return;
        }

        UploadFile file = uploadFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            log.warn("[UploadQueue] File not found: {}", fileId);
            return;
        }

        if (file.getStatus() == FileStatus.AVAILABLE) {
            log.info("[UploadQueue] File already available: {}", fileId);
            return;
        }

        try {
            log.info("[UploadQueue] Uploading to S3 | key: {}", file.getS3Key());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getS3Key())
                    .contentType(file.getContentType())
                    .acl(file.isPublic() ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.PRIVATE)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            file.setStatus(FileStatus.AVAILABLE);
            uploadFileRepository.save(file);

            log.info("[UploadQueue] Successfully uploaded file: {} to S3", fileId);
        } catch (Exception e) {
            log.error("[UploadQueue] Failed to upload file: {} to S3", fileId, e);
            throw new RuntimeException("S3 upload failed for file: " + fileId, e);
        }
    }
}
