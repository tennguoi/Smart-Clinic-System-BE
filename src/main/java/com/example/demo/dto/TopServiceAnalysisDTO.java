package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopServiceAnalysisDTO {
    // Top dịch vụ theo lượt đặt lịch (Appointment)
    private List<TopServiceItemDTO> topByAppointments;
    
    // Top dịch vụ theo lượt khám thực tế (MedicalRecord)
    private List<TopServiceItemDTO> topByMedicalRecords;
    
    // Top dịch vụ theo doanh thu (Invoice)
    private List<TopServiceItemDTO> topByRevenue;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopServiceItemDTO {
        private String serviceId;
        private String serviceName;
        private String serviceCategory;
        private BigDecimal servicePrice;
        private String photoUrl;
        
        // Số lượng/doanh thu tùy loại
        private Long quantity;           // Cho Appointment & MedicalRecord
        private BigDecimal totalRevenue; // Cho Invoice
        private BigDecimal avgPrice;     // Giá trung bình
    }
}
