package com.shegami.hr_saas.modules.upload.dto;

import com.shegami.hr_saas.modules.upload.mapper.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "Content type (MIME) is required")
    private String contentType;

    @NotNull(message = "File type is required")
    private FileType fileType;
}
