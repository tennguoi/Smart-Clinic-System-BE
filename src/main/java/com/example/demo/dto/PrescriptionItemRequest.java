package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PrescriptionItemRequest(
        @NotBlank(message = "Medication name is required")
        @Size(max = 255, message = "Medication name cannot exceed 255 characters")
        String medicationName,
        
        @Size(max = 255, message = "Active ingredient cannot exceed 255 characters")
        String activeIngredient,
        
        @Size(max = 100, message = "Strength cannot exceed 100 characters")
        String strength,
        
        @Size(max = 100, message = "Dosage form cannot exceed 100 characters")
        String dosageForm,
        
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity,
        
        @Size(max = 50, message = "Unit cannot exceed 50 characters")
        String unit,
        
        @Size(max = 1000, message = "Dosage instructions cannot exceed 1000 characters")
        String dosageInstructions,
        
        @Size(max = 100, message = "Frequency cannot exceed 100 characters")
        String frequency,
        
        @Size(max = 100, message = "Duration cannot exceed 100 characters")
        String duration,
        
        @Size(max = 100, message = "Administration route cannot exceed 100 characters")
        String administrationRoute,
        
        @Size(max = 1000, message = "Special instructions cannot exceed 1000 characters")
        String specialInstructions,
        
        @Size(max = 1000, message = "Precautions cannot exceed 1000 characters")
        String precautions,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than 0")
        BigDecimal unitPrice,
        
        @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        String notes
) {}
