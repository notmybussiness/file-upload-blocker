package com.madrascheck.extensionblocker.file.storage;

import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileObjectStorage implements FileObjectStorage {

    private final Path rootPath;

    public LocalFileObjectStorage(@Value("${app.storage.local.root-path:./.data/uploads}") String rootPath) {
        this.rootPath = Paths.get(rootPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootPath);
        } catch (IOException e) {
            throw new IllegalStateException("failed to initialize local storage root", e);
        }
    }

    @Override
    public void put(String key, byte[] bytes, String contentType) {
        Path target = resolvePath(key);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new IllegalStateException("failed to store file to local storage", e);
        }
    }

    @Override
    public StoredObject get(String key) {
        Path target = resolvePath(key);
        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("stored file not found: key=" + key);
        }
        try {
            String contentType = Files.probeContentType(target);
            return new StoredObject(Files.readAllBytes(target), contentType);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read file from local storage", e);
        }
    }

    @Override
    public void delete(String key) {
        Path target = resolvePath(key);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // best effort cleanup for MVP
        }
    }

    private Path resolvePath(String key) {
        Path target = rootPath.resolve(key).normalize();
        if (!target.startsWith(rootPath)) {
            throw new IllegalArgumentException("invalid storage key");
        }
        return target;
    }
}
