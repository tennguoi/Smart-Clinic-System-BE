// Tạo file mới: src/main/java/com/example/demo/dto/billing/BillingUpdateDTO.java
package com.example.demo.dto.billing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BillingUpdateDTO(
        @NotEmpty(message = "Danh sách dịch vụ không được rỗng")
        @Valid
        List<BillingItemDTO> items
) {}