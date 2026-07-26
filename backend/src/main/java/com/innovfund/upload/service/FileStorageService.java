package com.innovfund.upload.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.innovfund.common.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final long MAX_BYTES = 25L * 1024 * 1024;
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "startup-logos", "startup-covers", "pitch-decks", "demo-videos",
            "post-media", "due-diligence-documents", "team-photos");

    private final Cloudinary cloudinary;

    public String upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("No file provided");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("File exceeds the 25MB upload limit");
        }
        String safeFolder = ALLOWED_FOLDERS.contains(folder) ? folder : "misc";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "innovatefund/" + safeFolder,
                    "resource_type", "auto"));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new BadRequestException("File upload failed: " + e.getMessage());
        }
    }
}
