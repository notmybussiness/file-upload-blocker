package com.madrascheck.extensionblocker.extension.service;

import com.madrascheck.extensionblocker.common.error.ResourceNotFoundException;
import com.madrascheck.extensionblocker.common.error.ValidationException;
import com.madrascheck.extensionblocker.custom.CustomExtension;
import com.madrascheck.extensionblocker.custom.CustomExtensionRepository;
import com.madrascheck.extensionblocker.extension.dto.PolicyResponse;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicy;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicyRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class ExtensionPolicyService {

    public static final int MAX_CUSTOM_EXTENSIONS = 200;

    private static final Pattern CUSTOM_EXTENSION_PATTERN = Pattern.compile("^[a-z0-9]{1,20}$");

    private final FixedExtensionPolicyRepository fixedExtensionPolicyRepository;
    private final CustomExtensionRepository customExtensionRepository;

    public ExtensionPolicyService(
            FixedExtensionPolicyRepository fixedExtensionPolicyRepository,
            CustomExtensionRepository customExtensionRepository
    ) {
        this.fixedExtensionPolicyRepository = fixedExtensionPolicyRepository;
        this.customExtensionRepository = customExtensionRepository;
    }

    @Transactional(readOnly = true)
    public PolicyResponse getPolicy() {
        List<PolicyResponse.FixedExtensionItem> fixedItems = fixedExtensionPolicyRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(it -> new PolicyResponse.FixedExtensionItem(it.getName(), it.isChecked()))
                .toList();

        List<PolicyResponse.CustomExtensionItem> customItems = customExtensionRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(it -> new PolicyResponse.CustomExtensionItem(it.getId(), it.getName()))
                .toList();

        return new PolicyResponse(
                fixedItems,
                new PolicyResponse.CustomExtensionBlock(customItems.size(), MAX_CUSTOM_EXTENSIONS, customItems)
        );
    }

    @Transactional
    public PolicyResponse.FixedExtensionItem updateFixedExtension(String rawName, boolean checked) {
        String name = normalize(rawName);
        FixedExtensionPolicy policy = fixedExtensionPolicyRepository.findById(name)
                .orElseThrow(() -> new ValidationException("unsupported fixed extension: " + rawName));

        policy.setChecked(checked);

        return new PolicyResponse.FixedExtensionItem(policy.getName(), policy.isChecked());
    }

    @Transactional
    public PolicyResponse.CustomExtensionItem addCustomExtension(String rawName) {
        String name = normalize(rawName);

        if (!CUSTOM_EXTENSION_PATTERN.matcher(name).matches()) {
            throw new ValidationException("custom extension length must be between 1 and 20 and use only lowercase letters or digits");
        }

        if (fixedExtensionPolicyRepository.existsById(name)) {
            throw new ValidationException("custom extension must not match fixed extensions");
        }

        if (customExtensionRepository.existsByName(name)) {
            throw new ValidationException("custom extension already exists");
        }

        if (customExtensionRepository.count() >= MAX_CUSTOM_EXTENSIONS) {
            throw new ValidationException("custom extension limit exceeded: max 200");
        }

        CustomExtension saved = customExtensionRepository.save(new CustomExtension(name));

        return new PolicyResponse.CustomExtensionItem(saved.getId(), saved.getName());
    }

    @Transactional
    public void deleteCustomExtension(Long id) {
        CustomExtension extension = customExtensionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("custom extension not found: id=" + id));

        customExtensionRepository.delete(extension);
    }

    private String normalize(String rawName) {
        if (rawName == null) {
            throw new ValidationException("custom extension is required");
        }

        String normalized = rawName.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            throw new ValidationException("custom extension is required");
        }

        return normalized;
    }
}
