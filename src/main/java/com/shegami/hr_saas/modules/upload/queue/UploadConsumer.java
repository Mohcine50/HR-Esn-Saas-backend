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

        log.info("Received upload task for fileId: {} | size={} bytes", fileId,
                (content != null ? content.length : 0));

        if (fileId == null) {
            log.warn("Received null fileId");
            return;
        }

        UploadFile file = uploadFileRepository.findById(fileId).orElse(null);
        if (file == null) {
            log.warn("File not found in DB: {}", fileId);
            return;
        }

        if (file.getStatus() == FileStatus.AVAILABLE) {
            log.info("File already available in S3: {}", fileId);
            return;
        }

        try {
            log.info("[UploadConsumer] Uploading to S3 | key: {} | bucket: {} | contentType: {}",
                    file.getS3Key(), bucketName, file.getContentType());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getS3Key())
                    .contentType(file.getContentType())
                    .acl(file.isPublic() ? ObjectCannedACL.PUBLIC_READ : ObjectCannedACL.PRIVATE)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            file.setStatus(FileStatus.AVAILABLE);
            uploadFileRepository.save(file);

            log.info("[UploadConsumer] Successfully uploaded file: {} to S3 key: {}", fileId, file.getS3Key());
        } catch (Exception e) {
            log.error("[UploadConsumer] Failed to upload file: {} to bucket: {} | error: {}",
                    fileId, bucketName, e.getMessage());
            throw new RuntimeException("S3 upload failed for file: " + fileId + " in bucket: " + bucketName, e);
        }
    }
}
