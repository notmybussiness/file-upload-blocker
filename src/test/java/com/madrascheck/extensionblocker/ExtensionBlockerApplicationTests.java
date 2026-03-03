package com.madrascheck.extensionblocker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class ExtensionBlockerApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldExposeSplitPages() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client"));

        mockMvc.perform(get("/client"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/client/index.html"));

        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/index.html"));

        mockMvc.perform(get("/client/index.html"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/admin/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundForLegacyUnifiedApis() throws Exception {
        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/extensions/custom"))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/v1/extensions/fixed/exe"))
                .andExpect(status().isNotFound());
    }
}
