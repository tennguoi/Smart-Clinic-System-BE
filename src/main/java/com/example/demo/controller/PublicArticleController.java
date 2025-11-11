package com.example.demo.controller;
import com.example.demo.entity.Article;
import com.example.demo.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/public/articles")
public class PublicArticleController {
    @Autowired
    private ArticleService articleService;

    // Lấy danh sách bài viết (phân trang)
    @GetMapping
    public Page<Article> getAllArticles(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return articleService.getAllArticles(page, size);
    }
    @GetMapping("/{id}")
    public Article getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id);
    }

}
