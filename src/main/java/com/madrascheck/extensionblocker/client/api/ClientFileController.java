package com.madrascheck.extensionblocker.client.api;

import com.madrascheck.extensionblocker.file.dto.UploadedFileResponse;
import com.madrascheck.extensionblocker.file.service.DownloadedFile;
import com.madrascheck.extensionblocker.file.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client/files")
public class ClientFileController {

    private final FileService fileService;

    public ClientFileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadedFileResponse upload(@RequestPart("file") MultipartFile file) {
        return fileService.upload(file);
    }

    @GetMapping
    public List<UploadedFileResponse> listRecentFiles() {
        return fileService.listRecent();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable UUID id) {
        DownloadedFile downloadedFile = fileService.download(id);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (downloadedFile.contentType() != null && !downloadedFile.contentType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(downloadedFile.contentType());
            } catch (Exception ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }

        String encodedName = UriUtils.encode(downloadedFile.originalName(), StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename*=UTF-8''" + encodedName;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .contentType(mediaType)
                .contentLength(downloadedFile.bytes().length)
                .body(new ByteArrayResource(downloadedFile.bytes()));
    }
}
