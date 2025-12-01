// src/main/java/com/example/demo/dto/ReviewResponse.java
package com.example.demo.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Integer rating,
        String comment,
        String reviewerName,
        String reviewerEmail,
        LocalDateTime createdAt
) {}