package com.madrascheck.extensionblocker.file.service;

import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import com.madrascheck.extensionblocker.common.error.ValidationException;
import com.madrascheck.extensionblocker.custom.CustomExtensionRepository;
import com.madrascheck.extensionblocker.file.UploadedFile;
import com.madrascheck.extensionblocker.file.UploadedFileRepository;
import com.madrascheck.extensionblocker.file.dto.UploadedFileResponse;
import com.madrascheck.extensionblocker.file.storage.FileObjectStorage;
import com.madrascheck.extensionblocker.file.storage.StoredObject;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private final UploadedFileRepository uploadedFileRepository;
    private final FixedExtensionPolicyRepository fixedExtensionPolicyRepository;
    private final CustomExtensionRepository customExtensionRepository;
    private final FileObjectStorage fileObjectStorage;

    public FileService(
            UploadedFileRepository uploadedFileRepository,
            FixedExtensionPolicyRepository fixedExtensionPolicyRepository,
            CustomExtensionRepository customExtensionRepository,
            FileObjectStorage fileObjectStorage
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.fixedExtensionPolicyRepository = fixedExtensionPolicyRepository;
        this.customExtensionRepository = customExtensionRepository;
        this.fileObjectStorage = fileObjectStorage;
    }

    @Transactional
    public UploadedFileResponse upload(MultipartFile file) {
        validateFile(file);

        String originalName = normalizeOriginalName(file.getOriginalFilename());
        String extension = extractExtension(originalName);

        if (isBlocked(extension)) {
            throw new ValidationException("file extension is blocked: " + extension);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("failed to read upload file", e);
        }

        String storageKey = buildStorageKey(extension);
        String contentType = file.getContentType();

        fileObjectStorage.put(storageKey, bytes, contentType);

        UploadedFile uploadedFile = new UploadedFile(
                UUID.randomUUID(),
                originalName,
                extension,
                contentType,
                bytes.length,
                storageKey
        );

        try {
            UploadedFile saved = uploadedFileRepository.save(uploadedFile);
            return UploadedFileResponse.fromEntity(saved);
        } catch (RuntimeException ex) {
            fileObjectStorage.delete(storageKey);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<UploadedFileResponse> listRecent() {
        return uploadedFileRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(UploadedFileResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public DownloadedFile download(UUID id) {
        UploadedFile uploadedFile = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("uploaded file not found: id=" + id));

        StoredObject storedObject = fileObjectStorage.get(uploadedFile.getStorageKey());
        String contentType = uploadedFile.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = storedObject.contentType();
        }

        return new DownloadedFile(uploadedFile.getOriginalName(), contentType, storedObject.bytes());
    }

    String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot <= 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isBlocked(String extension) {
        if (extension.isBlank()) {
            return false;
        }

        Set<String> blocked = new HashSet<>();
        fixedExtensionPolicyRepository.findByCheckedTrue()
                .forEach(policy -> blocked.add(policy.getName()));
        customExtensionRepository.findAll()
                .forEach(custom -> blocked.add(custom.getName()));

        return blocked.contains(extension);
    }

    private String buildStorageKey(String extension) {
        String suffix = extension.isBlank() ? "" : "." + extension;
        return "uploads/" + UUID.randomUUID() + suffix;
    }

    private String normalizeOriginalName(String originalName) {
        if (originalName == null) {
            throw new ValidationException("original filename is required");
        }

        String normalized = originalName.replace('\\', '/');
        normalized = normalized.substring(normalized.lastIndexOf('/') + 1).trim();

        if (normalized.isBlank()) {
            throw new ValidationException("original filename is required");
        }

        return normalized;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("file is required");
        }
    }
}
