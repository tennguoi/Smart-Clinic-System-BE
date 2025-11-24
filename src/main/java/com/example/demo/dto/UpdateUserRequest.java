package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {
    private String fullName;
    private String phone;
    private String dob;
    private String gender;
    private String address;
    private Integer experienceYears;
    private String photoUrl;
    private String bio;
    private String password; // Thêm dòng này
}