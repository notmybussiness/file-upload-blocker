package com.madrascheck.extensionblocker.extension.dto;

import java.util.List;

public record PolicyResponse(List<FixedExtensionItem> fixed, CustomExtensionBlock custom) {

    public record FixedExtensionItem(String name, boolean checked) {
    }

    public record CustomExtensionBlock(long count, int max, List<CustomExtensionItem> items) {
    }

    public record CustomExtensionItem(Long id, String name) {
    }
}
