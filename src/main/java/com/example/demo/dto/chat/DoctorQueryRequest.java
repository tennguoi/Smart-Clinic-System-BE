package com.example.demo.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorQueryRequest {

    @NotBlank(message = "Session ID không được để trống")
    private String sessionId;

    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String text;
}
