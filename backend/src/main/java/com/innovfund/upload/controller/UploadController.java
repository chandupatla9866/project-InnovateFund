package com.innovfund.upload.controller;

import com.innovfund.upload.dto.UploadResponse;
import com.innovfund.upload.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final FileStorageService fileStorageService;

    @PostMapping
    public UploadResponse upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "folder", defaultValue = "misc") String folder) {
        return new UploadResponse(fileStorageService.upload(file, folder));
    }
}
