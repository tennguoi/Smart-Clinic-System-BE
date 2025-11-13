package com.example.demo.controller;

import com.example.demo.dto.ServiceRequest;
import com.example.demo.dto.ServiceResponse;
import com.example.demo.service.ServiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/services")
public class AdminServiceController {
    
    private final ServiceService serviceService;

    public AdminServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // Lấy tất cả dịch vụ (bao gồm cả inactive)
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(serviceService.getAllServices(page, size));
    }

    // Tạo dịch vụ mới
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Cập nhật dịch vụ
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable String id, 
            @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(serviceService.updateService(id, request));
    }

    // Xóa dịch vụ
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        serviceService.deleteService(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
