package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PrescriptionItemResponse(
        UUID itemId,
        String medicationName,
        String activeIngredient,
        String strength,
        String dosageForm,
        Integer quantity,
        String unit,
        String dosageInstructions,
        String frequency,
        String duration,
        String administrationRoute,
        String specialInstructions,
        String precautions,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String notes
) {}
