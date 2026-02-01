package com.example.demo.dto;

import com.example.demo.entity.Service;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class TopServiceDTO {
    private Service service;      // Chứa thông tin dịch vụ (tên, giá, ảnh...)
    private Long totalUsage;      // Tổng số lượng (quantity) - dùng cho các query đếm
    private BigDecimal totalRevenue; // Tổng doanh thu - dùng cho các query tính tổng tiền

    // Constructor cho các query đếm số lượng
    public TopServiceDTO(Service service, Long totalUsage) {
        this.service = service;
        this.totalUsage = totalUsage;
        this.totalRevenue = BigDecimal.ZERO;
    }

    // Constructor cho các query tính tổng doanh thu
    public TopServiceDTO(Service service, BigDecimal totalRevenue) {
        this.service = service;
        this.totalUsage = 0L;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }

    // Constructor đầy đủ (nếu cần)
    public TopServiceDTO(Service service, Long totalUsage, BigDecimal totalRevenue) {
        this.service = service;
        this.totalUsage = totalUsage != null ? totalUsage : 0L;
        this.totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
}