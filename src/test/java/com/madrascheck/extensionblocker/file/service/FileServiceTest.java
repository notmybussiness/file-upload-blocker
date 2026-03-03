package com.madrascheck.extensionblocker.file.service;

import com.madrascheck.extensionblocker.common.error.ValidationException;
import com.madrascheck.extensionblocker.custom.CustomExtension;
import com.madrascheck.extensionblocker.custom.CustomExtensionRepository;
import com.madrascheck.extensionblocker.file.UploadedFile;
import com.madrascheck.extensionblocker.file.UploadedFileRepository;
import com.madrascheck.extensionblocker.file.storage.FileObjectStorage;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicy;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private FixedExtensionPolicyRepository fixedExtensionPolicyRepository;

    @Mock
    private CustomExtensionRepository customExtensionRepository;

    @Mock
    private FileObjectStorage fileObjectStorage;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileService(uploadedFileRepository, fixedExtensionPolicyRepository, customExtensionRepository, fileObjectStorage);
    }

    @Test
    void uploadShouldBlockWhenFixedExtensionIsChecked() {
        when(fixedExtensionPolicyRepository.findByCheckedTrue()).thenReturn(List.of(new FixedExtensionPolicy("exe", true)));
        when(customExtensionRepository.findAll()).thenReturn(List.of());

        MockMultipartFile file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", "x".getBytes());

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("blocked: exe");

        verify(fileObjectStorage, never()).put(anyString(), any(), anyString());
    }

    @Test
    void uploadShouldBlockWhenCustomExtensionExists() {
        when(fixedExtensionPolicyRepository.findByCheckedTrue()).thenReturn(List.of());
        when(customExtensionRepository.findAll()).thenReturn(List.of(new CustomExtension("sh")));

        MockMultipartFile file = new MockMultipartFile("file", "install.SH", "application/octet-stream", "x".getBytes());

        assertThatThrownBy(() -> fileService.upload(file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("blocked: sh");
    }

    @Test
    void uploadShouldSaveWhenExtensionIsAllowed() {
        when(fixedExtensionPolicyRepository.findByCheckedTrue()).thenReturn(List.of());
        when(customExtensionRepository.findAll()).thenReturn(List.of());
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "photo.JPG", "image/jpeg", "abc".getBytes());

        var response = fileService.upload(file);

        assertThat(response.originalName()).isEqualTo("photo.JPG");
        assertThat(response.extension()).isEqualTo("jpg");
        verify(fileObjectStorage).put(anyString(), any(), anyString());
    }

    @Test
    void uploadShouldAllowNoExtensionFile() {
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "README", "text/plain", "abc".getBytes());

        var response = fileService.upload(file);

        assertThat(response.extension()).isEmpty();
    }

    @Test
    void uploadShouldDeleteStoredObjectWhenDatabaseSaveFails() {
        when(fixedExtensionPolicyRepository.findByCheckedTrue()).thenReturn(List.of());
        when(customExtensionRepository.findAll()).thenReturn(List.of());
        when(uploadedFileRepository.save(any(UploadedFile.class))).thenThrow(new RuntimeException("db failure"));

        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "abc".getBytes());

        assertThatThrownBy(() -> fileService.upload(file)).isInstanceOf(RuntimeException.class);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileObjectStorage).put(keyCaptor.capture(), any(), anyString());
        verify(fileObjectStorage).delete(keyCaptor.getValue());
    }
}
