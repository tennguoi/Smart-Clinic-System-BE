package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateEmailTemplateRequest {

    @NotBlank(message = "Tiêu đề email không được để trống")
    @Size(max = 255, message = "Tiêu đề email không được vượt quá 255 ký tự")
    private String subject;

    @NotBlank(message = "Nội dung email không được để trống")
    private String content;

    private String bodyContent; // Editable body content from React Quill

    private Boolean isHtml;
}
