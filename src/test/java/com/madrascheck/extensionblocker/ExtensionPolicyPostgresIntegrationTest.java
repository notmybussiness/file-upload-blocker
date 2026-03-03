package com.madrascheck.extensionblocker;

import com.madrascheck.extensionblocker.custom.CustomExtensionRepository;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicy;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ExtensionPolicyPostgresIntegrationTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\\\"id\\\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FixedExtensionPolicyRepository fixedExtensionPolicyRepository;

    @Autowired
    private CustomExtensionRepository customExtensionRepository;

    @BeforeEach
    void setUp() {
        customExtensionRepository.deleteAll();

        fixedExtensionPolicyRepository.findAll().forEach(policy -> {
            policy.setChecked(false);
            fixedExtensionPolicyRepository.save(policy);
        });
    }

    @Test
    void fixedExtensionStateShouldPersistAfterPatch() throws Exception {
        mockMvc.perform(patch("/api/v1/extensions/fixed/exe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checked\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checked").value(true));

        FixedExtensionPolicy exe = fixedExtensionPolicyRepository.findById("exe").orElseThrow();
        assertThat(exe.isChecked()).isTrue();
    }

    @Test
    void customExtensionShouldBeVisibleAfterAdd() throws Exception {
        mockMvc.perform(post("/api/v1/extensions/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"sh\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("sh"));

        mockMvc.perform(get("/api/v1/extensions/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custom.items[0].name").value("sh"))
                .andExpect(jsonPath("$.custom.count").value(1));
    }

    @Test
    void customExtensionShouldBeRemovedAfterDelete() throws Exception {
        String body = mockMvc.perform(post("/api/v1/extensions/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"sh\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ID_PATTERN.matcher(body);
        assertThat(matcher.find()).isTrue();
        long id = Long.parseLong(matcher.group(1));

        mockMvc.perform(delete("/api/v1/extensions/custom/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/extensions/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custom.count").value(0));
    }
}
