package com.madrascheck.extensionblocker;

import com.madrascheck.extensionblocker.custom.CustomExtensionRepository;
import com.madrascheck.extensionblocker.file.UploadedFileRepository;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = {
        "app.storage.type=local",
        "app.storage.local.root-path=build/test-upload-storage"
})
class FileUploadPostgresIntegrationTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\":\\\"([0-9a-f\\-]+)\\\"");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FixedExtensionPolicyRepository fixedExtensionPolicyRepository;

    @Autowired
    private CustomExtensionRepository customExtensionRepository;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @BeforeEach
    void setUp() throws IOException {
        uploadedFileRepository.deleteAll();
        customExtensionRepository.deleteAll();
        fixedExtensionPolicyRepository.findAll().forEach(policy -> {
            policy.setChecked(false);
            fixedExtensionPolicyRepository.save(policy);
        });

        Path root = Path.of("build/test-upload-storage");
        if (Files.exists(root)) {
            Files.walk(root)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }

    @Test
    void uploadListAndDownloadShouldWork() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "hello".getBytes());

        String uploadResponse = mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalName").value("sample.txt"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalName").value("sample.txt"))
                .andExpect(jsonPath("$[0].extension").value("txt"));

        Matcher matcher = ID_PATTERN.matcher(uploadResponse);
        assertThat(matcher.find()).isTrue();
        String id = matcher.group(1);

        mockMvc.perform(get("/api/v1/files/" + id + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename*=UTF-8''sample.txt"))
                .andExpect(header().string("Content-Type", MediaType.TEXT_PLAIN_VALUE));
    }

    @Test
    void uploadShouldBeRejectedWhenFixedExtensionIsBlocked() throws Exception {
        mockMvc.perform(patch("/api/v1/extensions/fixed/exe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checked\":true}"))
                .andExpect(status().isOk());

        MockMultipartFile blockedFile = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "abc".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(blockedFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("file extension is blocked: exe"));
    }

    @Test
    void uploadShouldBeRejectedWhenCustomExtensionIsBlocked() throws Exception {
        mockMvc.perform(post("/api/v1/extensions/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"sh\"}"))
                .andExpect(status().isCreated());

        MockMultipartFile blockedFile = new MockMultipartFile("file", "deploy.sh", "application/octet-stream", "abc".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(blockedFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("file extension is blocked: sh"));
    }
}
