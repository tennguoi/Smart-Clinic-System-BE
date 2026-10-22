package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ClinicRequest {

    @Size(max = 255, message = "Tên phòng khám không vượt quá 255 ký tự")
    private String name;

    @Size(max = 500, message = "Địa chỉ không vượt quá 500 ký tự")
    private String address;

    @Pattern(regexp = "^(0|\\+84)?\\d{9,10}$|^$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Size(max = 255, message = "Website không vượt quá 255 ký tự")
    private String website;

    private String logoUrl;

    private LocalTime morningStartTime;

    private LocalTime morningEndTime;

    private LocalTime afternoonStartTime;

    private LocalTime afternoonEndTime;
}
