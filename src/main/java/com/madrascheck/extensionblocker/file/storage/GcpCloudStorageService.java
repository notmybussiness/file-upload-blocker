package com.madrascheck.extensionblocker.file.storage;

import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcp")
public class GcpCloudStorageService implements FileObjectStorage {

    private final S3Client s3Client;
    private final String bucketName;

    public GcpCloudStorageService(
            @Value("${cloud.gcp.storage.bucket}") String bucketName,
            @Value("${cloud.gcp.storage.endpoint:https://storage.googleapis.com}") String endpoint,
            @Value("${cloud.gcp.storage.region:us-east1}") String region) {

        this.bucketName = bucketName;
        // GCS는 S3 API와 호환되므로, HMAC Keys가 시스템 환경변수(AWS_ACCESS_KEY_ID 등)로
        // 주입되면 AWS SDK를 통해 GCS와 통신할 수 있습니다.
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(software.amazon.awssdk.regions.Region.of(region))
                .build();
    }

    @Override
    public void put(String key, byte[] bytes, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("failed to store file to GCP Cloud Storage", e);
        }
    }

    @Override
    public StoredObject get(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(getObjectRequest);
            byte[] bytes = objectAsBytes.asByteArray();
            String contentType = objectAsBytes.response().contentType();
            return new StoredObject(bytes, contentType != null ? contentType : "application/octet-stream");
        } catch (Exception e) {
            throw new ResourceNotFoundException("stored file not found in GCS: key=" + key);
        }
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception ignored) {
            // best effort cleanup
        }
    }
}
