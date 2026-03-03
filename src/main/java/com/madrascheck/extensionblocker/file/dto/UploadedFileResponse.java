package com.madrascheck.extensionblocker.file.dto;

import com.madrascheck.extensionblocker.file.UploadedFile;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadedFileResponse(
        UUID id,
        String originalName,
        String extension,
        long sizeBytes,
        String contentType,
        LocalDateTime createdAt
) {
    public static UploadedFileResponse fromEntity(UploadedFile uploadedFile) {
        return new UploadedFileResponse(
                uploadedFile.getId(),
                uploadedFile.getOriginalName(),
                uploadedFile.getExtension(),
                uploadedFile.getSizeBytes(),
                uploadedFile.getContentType(),
                uploadedFile.getCreatedAt()
        );
    }
}
