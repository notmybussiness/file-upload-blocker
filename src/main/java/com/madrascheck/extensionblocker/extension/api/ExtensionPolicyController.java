package com.madrascheck.extensionblocker.extension.api;

import com.madrascheck.extensionblocker.extension.dto.CreateCustomExtensionRequest;
import com.madrascheck.extensionblocker.extension.dto.PolicyResponse;
import com.madrascheck.extensionblocker.extension.dto.UpdateFixedExtensionRequest;
import com.madrascheck.extensionblocker.extension.service.ExtensionPolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/extensions")
public class ExtensionPolicyController {

    private final ExtensionPolicyService extensionPolicyService;

    public ExtensionPolicyController(ExtensionPolicyService extensionPolicyService) {
        this.extensionPolicyService = extensionPolicyService;
    }

    @GetMapping("/policy")
    public PolicyResponse getPolicy() {
        return extensionPolicyService.getPolicy();
    }

    @PatchMapping("/fixed/{name}")
    public PolicyResponse.FixedExtensionItem updateFixedExtension(
            @PathVariable String name,
            @Valid @RequestBody UpdateFixedExtensionRequest request
    ) {
        return extensionPolicyService.updateFixedExtension(name, request.checked());
    }

    @PostMapping("/custom")
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse.CustomExtensionItem addCustomExtension(
            @Valid @RequestBody CreateCustomExtensionRequest request
    ) {
        return extensionPolicyService.addCustomExtension(request.name());
    }

    @DeleteMapping("/custom/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomExtension(@PathVariable Long id) {
        extensionPolicyService.deleteCustomExtension(id);
    }
}
