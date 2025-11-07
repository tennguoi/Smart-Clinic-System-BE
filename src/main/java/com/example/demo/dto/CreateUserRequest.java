package com.example.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được vượt quá 255 ký tự")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Size(min = 10, max = 10, message = "Số điện thoại phải có đúng 10 số")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại chỉ được chứa số")
    private String phone;

    @NotNull(message = "Ngày sinh không được để trống")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dob;

    @NotBlank(message = "Giới tính không được để trống")
    @Pattern(regexp = "^(male|female|other)$", flags = Pattern.Flag.CASE_INSENSITIVE, 
             message = "Giới tính phải là: male, female hoặc other")
    private String gender;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @Min(value = 0, message = "Số năm kinh nghiệm không được âm")
    @Max(value = 100, message = "Số năm kinh nghiệm không hợp lệ")
    private Integer experienceYears;

    @Size(max = 255, message = "URL ảnh không được vượt quá 255 ký tự")
    private String photoUrl;

    @NotBlank(message = "Giới thiệu không được để trống")
    @Size(max = 1000, message = "Giới thiệu không được vượt quá 1000 ký tự")
    private String bio;

    // role names, e.g. ["admin"], ["bac_si"], ["tiep_tan"]
    private List<String> roles;
}
