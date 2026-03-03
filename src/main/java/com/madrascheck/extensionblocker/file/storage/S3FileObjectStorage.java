package com.madrascheck.extensionblocker.file.storage;

import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3FileObjectStorage implements FileObjectStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileObjectStorage(@Value("${app.storage.s3.bucket}") String bucket) {
        this.s3Client = S3Client.builder().build();
        this.bucket = bucket;
    }

    @Override
    public void put(String key, byte[] bytes, String contentType) {
        PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key);
        if (contentType != null && !contentType.isBlank()) {
            builder = builder.contentType(contentType);
        }

        s3Client.putObject(builder.build(), RequestBody.fromBytes(bytes));
    }

    @Override
    public StoredObject get(String key) {
        try {
            ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(bucket).key(key).build()
            );
            return new StoredObject(object.asByteArray(), object.response().contentType());
        } catch (NoSuchKeyException ex) {
            throw new ResourceNotFoundException("stored file not found: key=" + key);
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (Exception ignored) {
            // best effort cleanup for MVP
        }
    }
}
