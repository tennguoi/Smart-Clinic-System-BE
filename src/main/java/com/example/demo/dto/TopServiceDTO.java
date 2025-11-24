package com.example.demo.dto;

import com.example.demo.entity.Service;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopServiceDTO {
    private Service service;  // Chứa tên, giá, ảnh...
    private Long totalUsage;  // Chứa tổng số lượng (quantity)
}