package com.madrascheck.extensionblocker.extension.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCustomExtensionRequest(@NotBlank String name) {
}
