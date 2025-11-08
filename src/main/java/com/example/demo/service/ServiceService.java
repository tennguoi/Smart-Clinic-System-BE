package com.example.demo.service;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    /**
     * Lấy tất cả dịch vụ đang hoạt động
     * @return Danh sách ServiceResponse
     */
    public List<ServiceResponse> getAllServices() {
        List<com.example.demo.entity.Service> services = serviceRepository.findByIsActiveTrue();

        return services.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Service entity sang ServiceResponse DTO
     * @param service Entity
     * @return ServiceResponse DTO
     */
    private ServiceResponse convertToResponse(com.example.demo.entity.Service service) {
        return new ServiceResponse(

                service.getName(),
                service.getDescription(),
                service.getCategory(),
                service.getPrice()
        );
    }
}