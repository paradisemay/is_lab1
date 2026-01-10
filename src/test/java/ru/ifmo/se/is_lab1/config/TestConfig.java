package ru.ifmo.se.is_lab1.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import ru.ifmo.se.is_lab1.service.FileStorageService;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public FileStorageService testFileStorageService() {
        return new FileStorageService() {
            private final Map<String, byte[]> storage = new HashMap<>();

            @Override
            public String uploadFile(String objectName, InputStream inputStream, String contentType) {
                try {
                    storage.put(objectName, inputStream.readAllBytes());
                    return objectName;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void deleteFile(String objectName) {
                storage.remove(objectName);
            }

            @Override
            public InputStream downloadFile(String objectName) {
                byte[] content = storage.get(objectName);
                if (content == null) {
                    throw new RuntimeException("File not found");
                }
                return new ByteArrayInputStream(content);
            }
        };
    }
}
