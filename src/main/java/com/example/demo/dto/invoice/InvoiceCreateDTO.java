package com.example.demo.dto.invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record InvoiceCreateDTO(

        @NotNull(message = "Thiếu thông tin bệnh nhân (patientId là bắt buộc)")
        UUID patientId,

        @NotEmpty(message = "Hóa đơn phải có ít nhất 1 dịch vụ")
        List<@Valid InvoiceItemDTO> items
) {}