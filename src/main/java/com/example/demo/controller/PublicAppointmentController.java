package com.example.demo.controller;

import com.example.demo.dto.appointment.AppointmentResponse;
import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RestController
@RequestMapping("/api/public/appointments")
public class PublicAppointmentController {
    private final AppointmentService service;
    private final AppointmentRepository repository;

    public PublicAppointmentController(AppointmentService service, AppointmentRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping("/quick-book")
    public ResponseEntity<?> quickBook(@RequestBody com.example.demo.dto.appointment.AppointmentRequest req) {
        try {
            System.out.println("==================== PUBLIC QUICK-BOOK REQUEST ====================");
            System.out.println("Patient Name: " + req.patientName());
            System.out.println("Phone: " + req.phone());
            System.out.println("Email: " + req.email());
            System.out.println("Appointment Time: " + req.appointmentTime());
            System.out.println("Service IDs: " + req.serviceIds());
            System.out.println("===================================================================");
            
            AppointmentResponse response = service.quickBook(req);
            
            System.out.println("==================== QUICK-BOOK RESPONSE ====================");
            System.out.println("Created Appointment Code: " + response.appointmentCode());
            System.out.println("=============================================================");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR IN QUICK-BOOK:");
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + " | Type: " + e.getClass().getName());
        }
    }

    @GetMapping("/track/{code}")
    public ResponseEntity<AppointmentResponse> trackByCode(@PathVariable String code) {
        System.out.println("==================== TRACKING REQUEST ====================");
        System.out.println("Tracking Code: " + code);
        System.out.println("==========================================================");
        
        Appointment appointment = repository.findByAppointmentCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch hẹn với mã: " + code));
        
        // Use reflection to call toResponse since it's private
        try {
            Method toResponse = service.getClass().getDeclaredMethod("toResponse", Appointment.class);
            toResponse.setAccessible(true);            
            AppointmentResponse response = (AppointmentResponse) toResponse.invoke(service, appointment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Error converting appointment to response", e);
        }
    }
}
