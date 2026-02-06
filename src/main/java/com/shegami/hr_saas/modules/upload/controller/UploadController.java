package com.shegami.hr_saas.modules.upload.controller;

import com.shegami.hr_saas.modules.upload.dto.UploadRequest;
import com.shegami.hr_saas.modules.upload.dto.UploadResponse;
import com.shegami.hr_saas.modules.upload.service.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@Slf4j
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("upload/initiate")
    public ResponseEntity<UploadResponse> initiate(@Valid @RequestBody UploadRequest request) {
        log.info("Initiating pre-signed upload for file: {}", request.getFileName());
        UploadResponse response = uploadService.initiateUpload(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PostMapping("upload/complete/{fileId}")
    public ResponseEntity<Void> complete(@PathVariable String fileId) {
        log.info("Finalizing upload for file ID: {}", fileId);
        uploadService.completeUpload(fileId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(@PathVariable String fileId) {
        log.debug("Generating download URL for file ID: {}", fileId);
        String url = uploadService.generateDownloadUrl(fileId);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
