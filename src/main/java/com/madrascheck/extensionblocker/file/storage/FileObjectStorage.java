package com.madrascheck.extensionblocker.file.storage;

public interface FileObjectStorage {

    void put(String key, byte[] bytes, String contentType);

    StoredObject get(String key);

    void delete(String key);
}
