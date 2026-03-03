package com.madrascheck.extensionblocker.extension.api;

import com.madrascheck.extensionblocker.admin.api.AdminExtensionPolicyController;
import com.madrascheck.extensionblocker.common.error.GlobalExceptionHandler;
import com.madrascheck.extensionblocker.common.error.ValidationException;
import com.madrascheck.extensionblocker.extension.dto.PolicyResponse;
import com.madrascheck.extensionblocker.extension.service.ExtensionPolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminExtensionPolicyController.class)
@Import(GlobalExceptionHandler.class)
class ExtensionPolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExtensionPolicyService extensionPolicyService;

    @Test
    void getPolicyShouldReturnOk() throws Exception {
        PolicyResponse response = new PolicyResponse(
                List.of(new PolicyResponse.FixedExtensionItem("exe", true)),
                new PolicyResponse.CustomExtensionBlock(1, 200, List.of(new PolicyResponse.CustomExtensionItem(1L, "sh")))
        );
        when(extensionPolicyService.getPolicy()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/extensions/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixed[0].name").value("exe"))
                .andExpect(jsonPath("$.custom.count").value(1));
    }

    @Test
    void patchFixedShouldReturnOk() throws Exception {
        when(extensionPolicyService.updateFixedExtension(anyString(), anyBoolean()))
                .thenReturn(new PolicyResponse.FixedExtensionItem("exe", true));

        mockMvc.perform(patch("/api/v1/admin/extensions/fixed/exe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"checked\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("exe"))
                .andExpect(jsonPath("$.checked").value(true));
    }

    @Test
    void postCustomShouldReturnCreated() throws Exception {
        when(extensionPolicyService.addCustomExtension(anyString()))
                .thenReturn(new PolicyResponse.CustomExtensionItem(10L, "sh"));

        mockMvc.perform(post("/api/v1/admin/extensions/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"sh\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("sh"));
    }

    @Test
    void deleteCustomShouldReturnNoContent() throws Exception {
        doNothing().when(extensionPolicyService).deleteCustomExtension(anyLong());

        mockMvc.perform(delete("/api/v1/admin/extensions/custom/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnValidationErrorFormatWhenServiceThrowsValidationException() throws Exception {
        doThrow(new ValidationException("custom extension already exists"))
                .when(extensionPolicyService)
                .addCustomExtension(anyString());

        mockMvc.perform(post("/api/v1/admin/extensions/custom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"sh\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("custom extension already exists"));
    }
}
