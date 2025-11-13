package com.example.demo.service;

import com.example.demo.dto.ArticleRequest;
import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {
    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // Public methods - Không cần authentication
    public List<ArticleResponse> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        return toResponse(article);
    }

    public List<ArticleResponse> getArticlesByCategory(String category) {
        return articleRepository.findByCategory(category).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ArticleResponse> searchArticlesByTitle(String title) {
        return articleRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Admin methods - Yêu cầu authentication
    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        Article article = Article.builder()
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .author(request.author())
                .publishedAt(LocalDateTime.now())
                .source(request.source())
                .image(request.image())
                .build();
        
        Article saved = articleRepository.save(article);
        return toResponse(saved);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        
        article.setTitle(request.title());
        article.setContent(request.content());
        article.setCategory(request.category());
        article.setAuthor(request.author());
        article.setSource(request.source());
        article.setImage(request.image());
        
        Article updated = articleRepository.save(article);
        return toResponse(updated);
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new IllegalArgumentException("Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }

    private ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getCategory(),
                article.getAuthor(),
                article.getPublishedAt(),
                article.getSource(),
                article.getImage()
        );
    }
}
