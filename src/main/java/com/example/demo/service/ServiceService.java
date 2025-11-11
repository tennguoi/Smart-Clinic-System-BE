package com.example.demo.service;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Lấy tất cả dịch vụ đang hoạt động với phân trang
     */
    public Page<ServiceResponse> getAllServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.example.demo.entity.Service> services = serviceRepository.findByIsActiveTrue(pageable);
        return services.map(this::convertToResponse);
    }

    /**
     * Tìm kiếm dịch vụ theo category (có phân trang)
     */
    public Page<ServiceResponse> getServicesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // Chuyển string sang enum
        com.example.demo.entity.Service.ServiceCategory serviceCategory;
        try {
            serviceCategory = com.example.demo.entity.Service.ServiceCategory.valueOf(category);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Danh mục không hợp lệ. Hãy chọn: Consultation, Test hoặc Procedure.");
        }

        Page<com.example.demo.entity.Service> services =
                serviceRepository.findByCategoryAndIsActiveTrue(serviceCategory, pageable);

        return services.map(this::convertToResponse);
    }

    /**
     * Convert từ Entity sang DTO
     */
    private ServiceResponse convertToResponse(com.example.demo.entity.Service service) {
        return new ServiceResponse(
                service.getServiceId(),
                service.getName(),
                service.getDescription(),
                service.getCategory(),
                service.getPrice(),
                service.getIsActive()
        );
    }
}
