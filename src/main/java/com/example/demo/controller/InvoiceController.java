// src/main/java/com/example/demo/controller/InvoiceController.java
package com.example.demo.controller;

import com.example.demo.dto.invoice.*;
import com.example.demo.service.InvoiceService;
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
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // XEM DANH SÁCH + TÌM KIẾM
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BAC_SI', 'TIEP_TAN')")
    public ResponseEntity<Page<InvoiceResponseDTO>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(invoiceService.getAll(pageable, search));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BAC_SI', 'TIEP_TAN')")
    public ResponseEntity<InvoiceResponseDTO> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(invoiceService.getById(id));
    }

    // TẠO HÓA ĐƠN MỚI
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
    public ResponseEntity<InvoiceResponseDTO> create(@Valid @RequestBody InvoiceCreateDTO dto) {
        return ResponseEntity.ok(invoiceService.createInvoice(dto));
    }

    // THANH TOÁN HÓA ĐƠN
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'TIEP_TAN')")
    public ResponseEntity<InvoiceResponseDTO> pay(
            @PathVariable UUID id,
            @RequestBody Map<String, BigDecimal> request) {

        BigDecimal paidAmount = request.get("paidAmount");
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0");
        }
        return ResponseEntity.ok(invoiceService.payInvoice(id, paidAmount));
    }
}