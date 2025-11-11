package com.example.demo.controller;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.service.ServiceService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/services")
public class PublicServiceController {
    
    private final ServiceService serviceService;

    public PublicServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // Lấy tất cả dịch vụ đang active
    @GetMapping
    public ResponseEntity<Page<ServiceResponse>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(serviceService.getAllActiveServices(page, size));
    }

    // Lấy dịch vụ theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable String id) {
        return ResponseEntity.ok(serviceService.getServiceById(id));
    }

    // Tìm kiếm theo category
    @GetMapping("/search")
    public ResponseEntity<Page<ServiceResponse>> searchByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(serviceService.getActiveServicesByCategory(category, page, size));
    }
}
