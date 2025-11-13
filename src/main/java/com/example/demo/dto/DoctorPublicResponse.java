package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPublicResponse {
    private UUID userId;
    private String fullName;
    private String email;
    private String phone;
    private String photoUrl;
    private String bio;
    private Integer experienceYears;
    private String specialization; // Có thể thêm field này nếu cần
}
