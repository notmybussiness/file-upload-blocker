package com.madrascheck.extensionblocker.extension.service;

import com.madrascheck.extensionblocker.common.error.ValidationException;
import com.madrascheck.extensionblocker.custom.CustomExtension;
import com.madrascheck.extensionblocker.custom.CustomExtensionRepository;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicy;
import com.madrascheck.extensionblocker.fixed.FixedExtensionPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtensionPolicyServiceTest {

    @Mock
    private FixedExtensionPolicyRepository fixedExtensionPolicyRepository;

    @Mock
    private CustomExtensionRepository customExtensionRepository;

    private ExtensionPolicyService extensionPolicyService;

    @BeforeEach
    void setUp() {
        extensionPolicyService = new ExtensionPolicyService(fixedExtensionPolicyRepository, customExtensionRepository);
    }

    @Test
    void updateFixedExtensionShouldUpdateChecked() {
        FixedExtensionPolicy fixed = new FixedExtensionPolicy("exe", false);
        when(fixedExtensionPolicyRepository.findById("exe")).thenReturn(Optional.of(fixed));

        var result = extensionPolicyService.updateFixedExtension("exe", true);

        assertThat(result.name()).isEqualTo("exe");
        assertThat(result.checked()).isTrue();
        assertThat(fixed.isChecked()).isTrue();
    }

    @Test
    void addCustomExtensionShouldFailWhenLengthOver20() {
        String longName = "abcdefghijklmnopqrstu";

        assertThatThrownBy(() -> extensionPolicyService.addCustomExtension(longName))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("length must be between 1 and 20");
    }

    @Test
    void addCustomExtensionShouldFailWhenDuplicate() {
        when(fixedExtensionPolicyRepository.existsById("sh")).thenReturn(false);
        when(customExtensionRepository.existsByName("sh")).thenReturn(true);

        assertThatThrownBy(() -> extensionPolicyService.addCustomExtension("sh"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void addCustomExtensionShouldFailWhenLimitExceeded() {
        when(fixedExtensionPolicyRepository.existsById("sh")).thenReturn(false);
        when(customExtensionRepository.existsByName("sh")).thenReturn(false);
        when(customExtensionRepository.count()).thenReturn((long) ExtensionPolicyService.MAX_CUSTOM_EXTENSIONS);

        assertThatThrownBy(() -> extensionPolicyService.addCustomExtension("sh"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("max 200");
    }

    @Test
    void addCustomExtensionShouldFailWhenMatchesFixedExtension() {
        when(fixedExtensionPolicyRepository.existsById("exe")).thenReturn(true);

        assertThatThrownBy(() -> extensionPolicyService.addCustomExtension("EXE"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("must not match fixed");
    }

    @Test
    void addCustomExtensionShouldNormalizeAndSave() {
        when(fixedExtensionPolicyRepository.existsById("sh")).thenReturn(false);
        when(customExtensionRepository.existsByName("sh")).thenReturn(false);
        when(customExtensionRepository.count()).thenReturn(0L);
        when(customExtensionRepository.save(any(CustomExtension.class))).thenAnswer(invocation -> {
            CustomExtension extension = invocation.getArgument(0);
            return new CustomExtension(extension.getName());
        });

        extensionPolicyService.addCustomExtension("  SH  ");

        verify(customExtensionRepository).save(any(CustomExtension.class));
    }
}
