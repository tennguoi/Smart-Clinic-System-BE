package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping("/quick-book")
    public ResponseEntity<AppointmentResponse> quickBook(@RequestBody AppointmentRequest req) {
        return ResponseEntity.ok(service.quickBook(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<List<AppointmentResponse>> list(@RequestParam(name = "status", required = false) Status status) {
        Status s = status != null ? status : Status.PENDING;
        return ResponseEntity.ok(service.listByStatus(s));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<AppointmentResponse> updateStatus(@PathVariable UUID id, @RequestParam Status status) {
        return ResponseEntity.ok(service.updateStatus(id, status));
    }
}
