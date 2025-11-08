package com.example.demo.controller;

import com.example.demo.dto.ServiceResponse;
import com.example.demo.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/services")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;


    @GetMapping
    public ResponseEntity<?> getAllServices() {
        try {
            List<ServiceResponse> services = serviceService.getAllServices();
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