package com.madrascheck.extensionblocker.file.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Google Cloud Storage 네이티브 구현체.
 * 인증: GOOGLE_APPLICATION_CREDENTIALS 환경변수로 서비스 계정 JSON 키 경로를 지정하면 자동 인증됩니다.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcp")
public class GcpCloudStorageService implements FileObjectStorage {

    private final Storage storage;
    private final String bucketName;

    public GcpCloudStorageService(
            @Value("${cloud.gcp.storage.bucket}") String bucketName,
            @Value("${cloud.gcp.storage.project-id:}") String projectId) {

        this.bucketName = bucketName;

        StorageOptions.Builder builder = StorageOptions.newBuilder();
        if (projectId != null && !projectId.isBlank()) {
            builder.setProjectId(projectId);
        }
        this.storage = builder.build().getService();
    }

    @Override
    public void put(String key, byte[] bytes, String contentType) {
        BlobId blobId = BlobId.of(bucketName, key);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

        try {
            storage.create(blobInfo, bytes);
        } catch (Exception e) {
            throw new IllegalStateException("failed to store file to GCP Cloud Storage", e);
        }
    }

    @Override
    public StoredObject get(String key) {
        BlobId blobId = BlobId.of(bucketName, key);

        try {
            byte[] bytes = storage.readAllBytes(blobId);
            com.google.cloud.storage.Blob blob = storage.get(blobId);
            String contentType = (blob != null && blob.getContentType() != null)
                    ? blob.getContentType()
                    : "application/octet-stream";
            return new StoredObject(bytes, contentType);
        } catch (Exception e) {
            throw new ResourceNotFoundException("stored file not found in GCS: key=" + key);
        }
    }

    @Override
    public void delete(String key) {
        BlobId blobId = BlobId.of(bucketName, key);

        try {
            storage.delete(blobId);
        } catch (Exception ignored) {
            // best effort cleanup
        }
    }
}
