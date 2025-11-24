package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class UserAdminResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private String phone;
    private String dob;
    private String gender;
    private String address;
    private Integer experienceYears;
    private String photoUrl;
    private String bio;
    private List<String> roles;
    private Boolean isVerified; // Thêm trường này để admin có thể chọn kích hoạt ngay
}
