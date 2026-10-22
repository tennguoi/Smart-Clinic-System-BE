package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
        @NotBlank(message = "Tiêu đề bài viết không được để trống")
        @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
        String title,

        @NotBlank(message = "Nội dung bài viết không được để trống")
        String content,

        @NotBlank(message = "Chuyên mục bài viết không được để trống")
        String category,

        @Size(max = 100, message = "Tên tác giả không được vượt quá 100 ký tự")
        String author,

        String source,
        String image
) {}
