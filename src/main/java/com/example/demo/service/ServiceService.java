package com.example.demo.service;

import com.example.demo.dto.ServiceRequest;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    // Public methods - Không cần authentication
    public Page<ServiceResponse> getAllActiveServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return serviceRepository.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<ServiceResponse> getActiveServicesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        com.example.demo.entity.Service.ServiceCategory serviceCategory = 
                com.example.demo.entity.Service.ServiceCategory.valueOf(category);
        return serviceRepository.findByCategoryAndIsActiveTrue(serviceCategory, pageable)
                .map(this::toResponse);
    }

    public ServiceResponse getServiceById(String id) {
        com.example.demo.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + id));
        return toResponse(service);
    }

    // Admin methods - Yêu cầu authentication
    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        com.example.demo.entity.Service service = new com.example.demo.entity.Service();
        service.setServiceId(UUID.randomUUID().toString());
        service.setName(request.name());
        service.setDescription(request.description());
        service.setCategory(com.example.demo.entity.Service.ServiceCategory.valueOf(request.category()));
        service.setPrice(request.price());
        service.setIsActive(request.isActive() != null ? request.isActive() : true);
        
        com.example.demo.entity.Service saved = serviceRepository.save(service);
        return toResponse(saved);
    }

    @Transactional
    public ServiceResponse updateService(String id, ServiceRequest request) {
        com.example.demo.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + id));
        
        service.setName(request.name());
        service.setDescription(request.description());
        service.setCategory(com.example.demo.entity.Service.ServiceCategory.valueOf(request.category()));
        service.setPrice(request.price());
        if (request.isActive() != null) {
            service.setIsActive(request.isActive());
        }
        
        com.example.demo.entity.Service updated = serviceRepository.save(service);
        return toResponse(updated);
    }

    @Transactional
    public void deleteService(String id) {
        if (!serviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Service not found with id: " + id);
        }
        serviceRepository.deleteById(id);
    }

    public Page<ServiceResponse> getAllServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return serviceRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private ServiceResponse toResponse(com.example.demo.entity.Service service) {
        return new ServiceResponse(
                service.getServiceId(),
                service.getName(),
                service.getDescription(),
                service.getCategory().name(),
                service.getPrice(),
                service.getIsActive()
        );
    }
}
