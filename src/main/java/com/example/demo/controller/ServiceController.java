package com.example.demo.controller;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/services")
@CrossOrigin(origins = {"http://localhost:5173" })
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public ResponseEntity<?> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        try {
            Page<ServiceResponse> services = serviceService.getAllServices(page, size);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new com.example.demo.dto.GenericResponse(
                            false,
                            "Lỗi khi lấy danh sách dịch vụ: " + e.getMessage()
                    ));
        }
    }
}