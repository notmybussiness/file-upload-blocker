package com.madrascheck.extensionblocker.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, UUID> {

    List<UploadedFile> findTop50ByOrderByCreatedAtDesc();
}
