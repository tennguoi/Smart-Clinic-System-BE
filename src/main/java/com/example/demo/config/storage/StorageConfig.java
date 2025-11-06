package com.example.demo.config.storage;

import com.example.demo.service.storage.FileStorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    CommandLineRunner init(FileStorageService fileStorageService) {
        return (args) -> {
            fileStorageService.init();
        };
    }
}
