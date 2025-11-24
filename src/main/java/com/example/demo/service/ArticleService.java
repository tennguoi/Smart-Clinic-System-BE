package com.example.demo.service;

import com.example.demo.dto.ArticleRequest;
import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    /* ================= PUBLIC APIs ================= */

    public Page<ArticleResponse> getAllArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(this::toResponse);
    }

    public Page<ArticleResponse> getArticlesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.findByCategoryOrderByPublishedAtDesc(category, pageable)
                .map(this::toResponse);
    }

    public Page<ArticleResponse> searchArticlesByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return articleRepository.searchByTitle(keyword, pageable)
                .map(this::toResponse);
    }

    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        return toResponse(article);
    }

    /* ================= ADMIN APIs ================= */

    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        Article article = Article.builder()
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .author(request.author())
                .source(request.source())
                .image(request.image())
                .publishedAt(LocalDateTime.now())
                .build();

        return toResponse(articleRepository.save(article));
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

        return toResponse(articleRepository.save(article));
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new IllegalArgumentException("Article not found with id: " + id);
        }
        articleRepository.deleteById(id);
    }

    /* ================= PRIVATE ================= */

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
