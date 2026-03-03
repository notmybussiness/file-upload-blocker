package com.madrascheck.extensionblocker.extension.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateFixedExtensionRequest(@NotNull Boolean checked) {
}
