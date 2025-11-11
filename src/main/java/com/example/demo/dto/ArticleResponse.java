package com.example.demo.dto;

import java.time.LocalDateTime;

public record ArticleResponse(
    Long id,
    String title,
    String content,
    String category,
    String author,
    LocalDateTime publishedAt,
    String source,
    String image
) {}
