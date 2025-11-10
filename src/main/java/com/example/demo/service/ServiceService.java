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
     * @param page Số trang (bắt đầu từ 0)
     * @param size Số lượng items mỗi trang
     * @return Page<ServiceResponse>
     */
    public Page<ServiceResponse> getAllServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.example.demo.entity.Service> services = serviceRepository.findByIsActiveTrue(pageable);

        return services.map(this::convertToResponse);
    }

    /**
     * Convert Service entity sang ServiceResponse DTO
     * @param service Entity
     * @return ServiceResponse DTO
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