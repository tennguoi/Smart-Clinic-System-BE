package com.example.demo.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileSystemStorageService implements FileStorageService {

    private final Path rootLocation;

    public FileSystemStorageService(@Value("${storage.location}") String storageLocation) {
        this.rootLocation = Paths.get(storageLocation);
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String save(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(newFilename))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException(
                        "Cannot store file outside current directory.");
            }
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile);
            }
            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }
}
