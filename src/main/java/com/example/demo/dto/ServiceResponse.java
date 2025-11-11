package com.example.demo.dto;

import java.math.BigDecimal;

public record ServiceResponse(
    String serviceId,
    String name,
    String description,
    String category,
    BigDecimal price,
    Boolean isActive
) {}
