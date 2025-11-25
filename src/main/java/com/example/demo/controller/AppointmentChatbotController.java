package com.example.demo.controller;

import com.example.demo.dto.appointment.AppointmentChatbotRequest;
import com.example.demo.dto.appointment.AppointmentChatbotResponse;
import com.example.demo.service.AppointmentChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/chatbot/")
@RequiredArgsConstructor
public class AppointmentChatbotController {

    private final AppointmentChatbotService service;

    @PostMapping("/appointment")
    public ResponseEntity<AppointmentChatbotResponse> createAppointment(
            @Valid @RequestBody AppointmentChatbotRequest request) {
        try {
            log.info("📝 Nhận request đặt lịch: {} - {}", request.getPatientName(), request.getPhone());

            AppointmentChatbotResponse response = service.createAppointment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("⚠️ Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(AppointmentChatbotResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("❌ Lỗi hệ thống", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AppointmentChatbotResponse.builder()
                            .success(false)
                            .message("Lỗi hệ thống: " + e.getMessage())
                            .build());
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppointmentChatbotResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = String.join("; ", errors.values());
        log.error("⚠️ Validation errors: {}", message);

        return ResponseEntity.badRequest()
                .body(AppointmentChatbotResponse.builder()
                        .success(false)
                        .message(message)
                        .build());
    }
}
