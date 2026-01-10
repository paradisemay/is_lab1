package ru.ifmo.se.is_lab1.service;

import java.io.InputStream;

public interface FileStorageService {
    /**
     * Uploads a file to the storage.
     * @param objectName The name of the object to store.
     * @param inputStream The content of the file.
     * @param contentType The content type of the file.
     * @return The path/key of the stored file.
     */
    String uploadFile(String objectName, InputStream inputStream, String contentType);

    /**
     * Deletes a file from the storage.
     * @param objectName The name/key of the file to delete.
     */
    void deleteFile(String objectName);

    /**
     * Retrieves a file from the storage.
     * @param objectName The name/key of the file.
     * @return The content stream.
     */
    InputStream downloadFile(String objectName);
}
