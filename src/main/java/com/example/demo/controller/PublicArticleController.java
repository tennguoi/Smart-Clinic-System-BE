package com.example.demo.controller;

import com.example.demo.dto.ArticleResponse;
import com.example.demo.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/articles")
public class PublicArticleController {
    
    private final ArticleService articleService;

    public PublicArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // Lấy tất cả bài viết
    @GetMapping
    public ResponseEntity<List<ArticleResponse>> getAllArticles() {
        return ResponseEntity.ok(articleService.getAllArticles());
    }

    // Lấy bài viết theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getArticleById(id));
    }

    // Lấy bài viết theo category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ArticleResponse>> getArticlesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(articleService.getArticlesByCategory(category));
    }

    // Tìm kiếm bài viết theo title
    @GetMapping("/search")
    public ResponseEntity<List<ArticleResponse>> searchArticles(@RequestParam String title) {
        return ResponseEntity.ok(articleService.searchArticlesByTitle(title));
    }
}
