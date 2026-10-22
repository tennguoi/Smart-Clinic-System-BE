package com.example.demo.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPrescriptionRequest {

    @NotNull(message = "ID hồ sơ bệnh án không được để trống")
    private UUID recordId;

    @NotBlank(message = "Danh sách thuốc không được để trống")
    private String drugs;

    @NotBlank(message = "Hướng dẫn sử dụng thuốc không được để trống")
    private String instructions;
}