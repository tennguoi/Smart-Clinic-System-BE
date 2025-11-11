package com.example.demo.controller;

import com.example.demo.entity.Article;
import com.example.demo.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // Lấy chi tiết bài viết
    @GetMapping("/{id}")
    public Article getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

    // Tạo bài viết mới
    @PostMapping
    public Article createArticle(@RequestBody Article article) {
        return articleService.createArticle(article);
    }

    // Tìm bài viết theo category
    @GetMapping("/category/{category}")
    public List<Article> getArticlesByCategory(@PathVariable String category) {
        return articleService.getArticlesByCategory(category);
    }

    // Tìm bài viết theo keyword trong tiêu đề
    @GetMapping("/search")
    public List<Article> searchArticles(@RequestParam String keyword) {
        return articleService.searchArticlesByTitle(keyword);
    }
}