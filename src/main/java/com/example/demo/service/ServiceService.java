package com.example.demo.service;

import com.example.demo.dto.ServiceRequest;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@org.springframework.stereotype.Service
public class ServiceService {
    private final ServiceRepository serviceRepository;
    private final String uploadDir = "uploads/services/"; // Thư mục lưu ảnh

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
        // Tạo thư mục nếu chưa có
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    // ==================== PUBLIC METHODS ====================

    public Page<ServiceResponse> getAllActiveServices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return serviceRepository.findByIsActiveTrue(pageable)
                .map(this::toResponse);
    }

    public Page<ServiceResponse> getActiveServicesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
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

    // ==================== ADMIN METHODS ====================

    /**
     * Lấy tất cả dịch vụ cho admin (8 bản ghi/trang)
     */
    public Page<ServiceResponse> getAllServicesForAdmin(int page, int size) {
        // Mặc định 8 bản ghi nếu không truyền size
        int pageSize = (size > 0) ? size : 8;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("name").ascending());
        return serviceRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        com.example.demo.entity.Service service = new com.example.demo.entity.Service();
        service.setServiceId(UUID.randomUUID().toString());
        service.setName(request.name());
        service.setDescription(request.description());
        service.setCategory(com.example.demo.entity.Service.ServiceCategory.valueOf(request.category()));
        service.setPrice(request.price());
        service.setIsActive(request.isActive() != null ? request.isActive() : true);
        service.setPhotoUrl(request.photoUrl());

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
        service.setPhotoUrl(request.photoUrl());

        com.example.demo.entity.Service updated = serviceRepository.save(service);
        return toResponse(updated);
    }

    /**
     * Cập nhật trạng thái nhanh (toggle active/inactive)
     */
    @Transactional
    public ServiceResponse toggleServiceStatus(String id) {
        com.example.demo.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + id));

        // Đảo ngược trạng thái
        service.setIsActive(!service.getIsActive());

        com.example.demo.entity.Service updated = serviceRepository.save(service);
        return toResponse(updated);
    }

    @Transactional
    public void deleteService(String id) {
        com.example.demo.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + id));

        // Xóa ảnh nếu có
        if (service.getPhotoUrl() != null && !service.getPhotoUrl().isEmpty()) {
            deletePhoto(service.getPhotoUrl());
        }

        serviceRepository.deleteById(id);
    }

    // ==================== PHOTO HANDLING ====================

    /**
     * Upload ảnh dịch vụ
     */
    public String uploadPhoto(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try {
            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // Lưu file
            Path targetPath = Paths.get(uploadDir + newFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL (có thể điều chỉnh theo cấu hình server)
            return "/uploads/services/" + newFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa ảnh
     */
    public void deletePhoto(String photoUrl) {
        try {
            if (photoUrl != null && photoUrl.startsWith("/uploads/services/")) {
                String filename = photoUrl.substring("/uploads/services/".length());
                Path filePath = Paths.get(uploadDir + filename);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error nhưng không throw exception
            System.err.println("Failed to delete photo: " + e.getMessage());
        }
    }

    /**
     * Cập nhật ảnh cho service
     */
    @Transactional
    public ServiceResponse updateServicePhoto(String id, MultipartFile file) {
        com.example.demo.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Service not found with id: " + id));

        // Xóa ảnh cũ nếu có
        if (service.getPhotoUrl() != null) {
            deletePhoto(service.getPhotoUrl());
        }

        // Upload ảnh mới
        String photoUrl = uploadPhoto(file);
        service.setPhotoUrl(photoUrl);

        com.example.demo.entity.Service updated = serviceRepository.save(service);
        return toResponse(updated);
    }
    public Page<ServiceResponse> searchServices(
            String name,
            String category,
            Boolean isActive,
            Double minPrice,
            Double maxPrice,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        com.example.demo.entity.Service.ServiceCategory categoryEnum = null;

        if (category != null && !category.isBlank()) {
            categoryEnum = com.example.demo.entity.Service.ServiceCategory.valueOf(category);
        }

        Page<com.example.demo.entity.Service> result = serviceRepository.searchServices(
                (name == null || name.isBlank()) ? null : name,
                categoryEnum,
                isActive,
                minPrice,
                maxPrice,
                pageable
        );

        return result.map(this::toResponse);
    }

    // ==================== HELPER METHODS ====================

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
                service.getIsActive(),
                service.getPhotoUrl()
        );
    }
}