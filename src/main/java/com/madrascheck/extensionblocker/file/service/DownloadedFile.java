package com.madrascheck.extensionblocker.file.service;

public record DownloadedFile(String originalName, String contentType, byte[] bytes) {
}
