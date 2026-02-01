package com.example.demo.dto;

public record ArticleRequest(
        String title,
        String content,
        String category,
        String author,
        String source,
        String image
) {}
