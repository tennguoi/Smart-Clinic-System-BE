// src/main/java/com/example/demo/dto/invoice/InvoiceCreateDTO.java
package com.example.demo.dto.invoice;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record InvoiceCreateDTO(

        @NotNull(message = "Thiếu thông tin bệnh nhân")
        UUID patientId,

        @NotEmpty(message = "Hóa đơn phải có ít nhất 1 dịch vụ")
        List<InvoiceItemDTO> items
) {}