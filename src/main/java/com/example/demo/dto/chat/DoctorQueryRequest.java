package com.example.demo.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DoctorQueryRequest {
    @NotBlank(message = "Nội dung không được để trống") String text;
}
