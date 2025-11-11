package com.example.demo.controller;

import com.example.demo.dto.AppointmentRequest;
import com.example.demo.dto.AppointmentResponse;
import com.example.demo.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/appointments")
public class PublicAppointmentController {
    private final AppointmentService service;

    public PublicAppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping("/quick-book")
    public ResponseEntity<AppointmentResponse> quickBook(@RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(service.quickBook(req));
    }
}
