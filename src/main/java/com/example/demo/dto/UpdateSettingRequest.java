package com.example.demo.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateSettingRequest {

    @Pattern(regexp = "^(vi|en)?$", message = "Ngôn ngữ chỉ chấp nhận 'vi' hoặc 'en'")
    private String language;

    @Pattern(regexp = "^(light|dark)?$", message = "Giao diện chỉ chấp nhận 'light' hoặc 'dark'")
    private String theme;
}
