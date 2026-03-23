package com.shegami.hr_saas.modules.upload.entity;

import com.shegami.hr_saas.modules.auth.entity.User;
import com.shegami.hr_saas.modules.upload.mapper.FileStatus;
import com.shegami.hr_saas.modules.upload.mapper.FileType;
import com.shegami.hr_saas.shared.entity.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Table(name="files")
@Entity
@Getter
@Setter
public class UploadFile extends BaseTenantEntity {

    private String fileName;
    private String s3Key;
    private String contentType;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(name = "public_url")
    private String publicUrl;


    @Id
    @Column(name = "file_id", nullable = false)
    private String fileId;
    @PrePersist
    public void generateFileId() {
        if (this.fileId == null) {
            this.fileId = "FILE-" + UUID.randomUUID();
        }
    }
}
