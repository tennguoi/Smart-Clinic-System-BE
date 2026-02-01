package com.example.demo.dto.prescription; // (Đặt trong package DTO của bạn)

import lombok.Data;
import java.util.UUID;

@Data
public class AddPrescriptionRequest {

    /**
     * ID của hồ sơ bệnh án (BẮT BUỘC).
     * Backend cần ID này để biết đơn thuốc này thuộc về hồ sơ nào.
     */
    private UUID recordId;

    /**
     * Tên thuốc (Nhiệm vụ của bạn)
     */
    private String drugs;

    /**
     * Hướng dẫn (Nhiệm vụ của bạn)
     */
    private String instructions;
}