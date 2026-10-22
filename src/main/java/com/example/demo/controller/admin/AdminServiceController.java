package com.example.demo.controller.admin;

import com.example.demo.dto.ServiceRequest;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/services")
@PreAuthorize("hasRole('ADMIN')")
public class AdminServiceController {

    private final ServiceService serviceService;

    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    /**
     * Lấy tất cả dịch vụ cho admin (8 bản ghi/trang mặc định)
     */
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {  // Mặc định 8 bản ghi
        return ResponseEntity.ok(serviceService.getAllServicesForAdmin(page, size));
    }
    /**
     * Tìm kiếm dịch vụ theo nhiều tiêu chí:
     * name, category, isActive, minPrice, maxPrice
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ServiceResponse>> searchServices(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Page<ServiceResponse> result = serviceService.searchServices(
                name,
                category,
                isActive,
                minPrice,
                maxPrice,
                page,
                size
        );

        return ResponseEntity.ok(result);
    }
    /**
     * Lấy chi tiết dịch vụ theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable String id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    /**
     * Tạo dịch vụ mới
     */
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật dịch vụ
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable String id,
            @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(id, request));
    }

    /**
     * Cập nhật trạng thái nhanh (toggle active/inactive)
     * Endpoint này cho phép bật/tắt trạng thái dịch vụ nhanh chóng
     */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ServiceResponse> toggleServiceStatus(@PathVariable String id) {
        ServiceResponse response = serviceService.toggleServiceStatus(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa dịch vụ
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        serviceService.deleteService(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ==================== PHOTO ENDPOINTS ====================

    /**
     * Upload ảnh dịch vụ (dùng khi tạo mới hoặc cập nhật)
     * Trả về URL của ảnh đã upload
     */
    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @RequestParam("file") MultipartFile file) {

        String photoUrl = serviceService.uploadPhoto(file);

        Map<String, String> response = new HashMap<>();
        response.put("photoUrl", photoUrl);
        response.put("message", "Photo uploaded successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật ảnh cho dịch vụ đã tồn tại
     */
    @PatchMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ServiceResponse> updateServicePhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {

        ServiceResponse response = serviceService.updateServicePhoto(id, file);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa ảnh của dịch vụ (set photoUrl = null)
     */
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<ServiceResponse> deleteServicePhoto(@PathVariable String id) {
        ServiceRequest request = new ServiceRequest(
                null, null, null, null, null, null
        );
        ServiceResponse currentService = serviceService.getServiceById(id);

        // Xóa ảnh cũ
        if (currentService.photoUrl() != null) {
            serviceService.deletePhoto(currentService.photoUrl());
        }

        // Cập nhật service với photoUrl = null
        ServiceRequest updateRequest = new ServiceRequest(
                currentService.name(),
                currentService.description(),
                currentService.category(),
                currentService.price(),
                currentService.isActive(),
                null  // Set photoUrl = null
        );

        ServiceResponse response = serviceService.updateService(id, updateRequest);
        return ResponseEntity.ok(response);
    }
}