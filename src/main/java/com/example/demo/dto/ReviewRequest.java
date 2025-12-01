// src/main/java/com/example/demo/dto/ReviewRequest.java
package com.example.demo.dto;

import jakarta.validation.constraints.*;

public record ReviewRequest(
        @Min(1) @Max(5) Integer rating,
        String comment,
        @NotBlank String reviewerName,
        @Email String reviewerEmail
) {}