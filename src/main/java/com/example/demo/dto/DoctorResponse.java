package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponse {
    private UUID doctorId;
    private String fullName;
    private String photoUrl;
    private String bio;
    private Integer experienceYears;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private LocalDate dob;
}

