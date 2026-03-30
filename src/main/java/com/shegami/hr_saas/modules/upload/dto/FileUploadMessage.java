package com.shegami.hr_saas.modules.upload.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadMessage implements Serializable {
    private String fileId;
    private byte[] content;
}
