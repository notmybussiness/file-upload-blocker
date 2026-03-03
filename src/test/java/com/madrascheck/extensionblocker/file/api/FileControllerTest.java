package com.madrascheck.extensionblocker.file.api;

import com.madrascheck.extensionblocker.common.error.GlobalExceptionHandler;
import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import com.madrascheck.extensionblocker.common.error.ValidationException;
import com.madrascheck.extensionblocker.file.dto.UploadedFileResponse;
import com.madrascheck.extensionblocker.file.service.DownloadedFile;
import com.madrascheck.extensionblocker.file.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileController.class)
@Import(GlobalExceptionHandler.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    void uploadShouldReturnBadRequestWhenFileMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/files"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("file is required"));
    }

    @Test
    void uploadShouldReturnValidationErrorWhenBlocked() throws Exception {
        when(fileService.upload(any())).thenThrow(new ValidationException("file extension is blocked: exe"));
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "abc".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void uploadShouldReturnCreatedWhenAllowed() throws Exception {
        UploadedFileResponse response = new UploadedFileResponse(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "sample.pdf",
                "pdf",
                3,
                "application/pdf",
                LocalDateTime.of(2026, 3, 3, 16, 0)
        );
        when(fileService.upload(any())).thenReturn(response);
        MockMultipartFile file = new MockMultipartFile("file", "sample.pdf", "application/pdf", "abc".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalName").value("sample.pdf"))
                .andExpect(jsonPath("$.extension").value("pdf"));
    }

    @Test
    void listShouldReturnFiles() throws Exception {
        when(fileService.listRecent()).thenReturn(List.of(
                new UploadedFileResponse(
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        "sample.pdf",
                        "pdf",
                        3,
                        "application/pdf",
                        LocalDateTime.of(2026, 3, 3, 16, 0)
                )
        ));

        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalName").value("sample.pdf"));
    }

    @Test
    void downloadShouldReturnAttachmentHeaders() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(fileService.download(id)).thenReturn(new DownloadedFile("sample.pdf", "application/pdf", "abc".getBytes()));

        mockMvc.perform(get("/api/v1/files/" + id + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename*=UTF-8''sample.pdf"))
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void downloadShouldReturn404WhenMissing() throws Exception {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(fileService.download(id)).thenThrow(new ResourceNotFoundException("uploaded file not found: id=" + id));

        mockMvc.perform(get("/api/v1/files/" + id + "/download"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
