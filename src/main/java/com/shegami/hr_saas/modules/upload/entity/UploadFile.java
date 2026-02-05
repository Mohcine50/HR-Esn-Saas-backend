package com.shegami.hr_saas.modules.upload.entity;

import com.shegami.hr_saas.modules.upload.mapper.FileStatus;
import com.shegami.hr_saas.modules.upload.mapper.FileType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Table(name="files")
@Entity
@Getter
@Setter
public class UploadFile {

    private String fileName;
    private String s3Key;
    private String contentType;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Enumerated(EnumType.STRING)
    private FileType fileType;


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
