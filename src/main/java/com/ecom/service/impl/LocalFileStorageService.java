package com.ecom.service.impl;

import com.ecom.config.FileStorageProperties;
import com.ecom.exception.BadRequestException;
import com.ecom.service.FileStorageService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// @Service - Disabled in favor of ImgBBService
// @RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private FileStorageProperties properties;
    
    public LocalFileStorageService() {
        // Default constructor for disabled service
    }
    
    public LocalFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    private Path rootLocation;

    @PostConstruct
    void init() {
        rootLocation = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize storage directory", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = "";
        int extensionIndex = originalName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            extension = originalName.substring(extensionIndex);
        }

        String filename = UUID.randomUUID() + extension;
        Path destination = rootLocation.resolve(filename);

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store file", ex);
        }

        return properties.getUploadDir() + "/" + filename;
    }

    @Override
    public void deleteFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        String sanitized = relativePath.replace("\\", "/");
        String filename = sanitized.substring(sanitized.lastIndexOf('/') + 1);
        Path filePath = rootLocation.resolve(filename);

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // ignore deletion failures to avoid blocking business flow
        }
    }
}

