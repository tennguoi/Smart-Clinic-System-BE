package com.example.demo.dto;

import java.math.BigDecimal;

public record ServiceRequest(
    String name,
    String description,
    String category,
    BigDecimal price,
    Boolean isActive
) {}
