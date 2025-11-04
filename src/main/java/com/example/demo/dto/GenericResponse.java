package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericResponse {
    private boolean success;
    private String message;
    private String token; // Thêm trường token để hỗ trợ trả về JWT

    // Constructor cho trường hợp không có token
    public GenericResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.token = null;
    }
}