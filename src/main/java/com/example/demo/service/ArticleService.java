package com.example.demo.service;

import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    // Lấy tất cả bài viết (phân trang)
    public Page<Article> getAllArticles(int page, int size) {
        return articleRepository.findAll(PageRequest.of(page, size));
    }

    // Lấy bài viết theo ID
    public Article getArticleById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
    }

    // Tạo bài viết mới
    public Article createArticle(Article article) {
        article.setPublishedAt(LocalDateTime.now());
        return articleRepository.save(article);
    }

    // Tìm bài viết theo category
    public List<Article> getArticlesByCategory(String category) {
        return articleRepository.findByCategory(category);
    }

    // Tìm bài viết theo keyword trong tiêu đề
    public List<Article> searchArticlesByTitle(String keyword) {
        return articleRepository.findByTitleContainingIgnoreCase(keyword);
    }
}