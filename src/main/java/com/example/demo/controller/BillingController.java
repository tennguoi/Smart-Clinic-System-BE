// src/main/java/com/example/demo/controller/BillingController.java
package com.example.demo.controller;

import com.example.demo.dto.billing.*;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasRole('TIEP_TAN')")
    public ResponseEntity<BillingResponseDTO> create(@Valid @RequestBody BillingCreateDTO dto) {
        return ResponseEntity.ok(billingService.create(dto));
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('TIEP_TAN')")
    public ResponseEntity<BillingResponseDTO> pay(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        BigDecimal paidAmount = new BigDecimal(body.get("paidAmount").toString());
        PaymentMethod method = PaymentMethod.valueOf(body.get("paymentMethod").toString());

        return ResponseEntity.ok(billingService.pay(id, paidAmount, method));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN')")
    public ResponseEntity<Page<BillingResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(billingService.getAll(pageable, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BAC_SI', 'TIEP_TAN')")
    public ResponseEntity<BillingResponseDTO> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(billingService.getById(id));
    }
}