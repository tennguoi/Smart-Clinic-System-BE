package com.example.demo.service;

import com.example.demo.dto.SettingResponse;
import com.example.demo.dto.UpdateSettingRequest;
import com.example.demo.entity.Setting;
import com.example.demo.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SettingService {

    @Autowired
    private SettingRepository settingRepository;

    /**
     * Get existing settings or create default settings for a user
     */
    public SettingResponse getOrCreateSetting(UUID userId) {
        Setting setting = settingRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Setting newSetting = Setting.builder()
                            .userId(userId)
                            .language("vi")
                            .theme("light")
                            .build();
                    return settingRepository.save(newSetting);
                });

        return mapToResponse(setting);
    }

    /**
     * Get settings for a user
     */
    public SettingResponse getSetting(UUID userId) {
        Setting setting = settingRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Settings not found for user"));
        return mapToResponse(setting);
    }

    /**
     * Update settings for a user
     */
    public SettingResponse updateSetting(UUID userId, UpdateSettingRequest request) {
        Setting setting = settingRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Settings not found for user"));

        if (request.getLanguage() != null) {
            setting.setLanguage(request.getLanguage());
        }
        if (request.getTheme() != null) {
            setting.setTheme(request.getTheme());
        }

        Setting updatedSetting = settingRepository.save(setting);
        return mapToResponse(updatedSetting);
    }

    /**
     * Map Setting entity to SettingResponse DTO
     */
    private SettingResponse mapToResponse(Setting setting) {
        return SettingResponse.builder()
                .settingId(setting.getSettingId())
                .userId(setting.getUserId())
                .language(setting.getLanguage())
                .theme(setting.getTheme())
                .build();
    }
}
