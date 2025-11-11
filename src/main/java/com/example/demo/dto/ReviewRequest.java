package com.example.demo.dto;

public record ReviewRequest(
    Integer rating,
    String comment,
    String reviewerName,
    String reviewerEmail
) {}
