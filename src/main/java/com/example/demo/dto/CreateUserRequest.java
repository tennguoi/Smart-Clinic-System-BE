package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    private String fullName;

    private String phone;
    private LocalDate dob;
    private String gender; // male, female, other
    private String address;
    private Integer experienceYears;
    private String photoUrl;
    private String bio;

    // role names, e.g. ["admin"], ["bác sĩ"], ["tiếp tân"]
    private List<String> roles;
}
