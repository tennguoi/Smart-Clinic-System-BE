package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {

    @Email(message = "Email không đúng định dạng")
    private String email;

    @Size(max = 255, message = "Họ tên không vượt quá 255 ký tự")
    private String fullName;

    @Pattern(regexp = "^(0|\\+84)?\\d{9,10}$|^$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    private String dob;

    @Pattern(regexp = "^(male|female|other)?$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Giới tính phải là: male, female hoặc other")
    private String gender;

    private String address;

    @Min(value = 0, message = "Số năm kinh nghiệm không được âm")
    @Max(value = 100, message = "Số năm kinh nghiệm không hợp lệ")
    private Integer experienceYears;

    private String photoUrl;

    @Size(max = 1000, message = "Giới thiệu không vượt quá 1000 ký tự")
    private String bio;

    private List<String> roles;

    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;

    private Boolean isVerified;

    private Boolean twoFactorEnabled;
}