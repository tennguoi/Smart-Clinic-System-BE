package com.example.demo.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSimpleResponse {
    private UUID doctorId;
    private String fullName;
    private String email;
    private String phone;
}

