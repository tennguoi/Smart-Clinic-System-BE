package com.example.demo.service;

import com.example.demo.dto.ArticleRequest;
import com.example.demo.dto.ArticleResponse;
import com.example.demo.entity.Article;
import com.example.demo.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor  // thay vì constructor thủ công
public class ArticleService {

    private final ArticleRepository articleRepository;

    /* ====================== PUBLIC METHODS (không cần auth) ====================== */

    // 1. Lấy tất cả bài viết + phân trang + sắp xếp mới nhất
    public Page<ArticleResponse> getAllArticles(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return articleRepository.findAllByOrderByPublishedAtDesc(pageable)
                .map(this::toResponse);
    }

    // 2. Lấy theo danh mục + phân trang + sắp xếp mới nhất
    public Page<ArticleResponse> getArticlesByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return articleRepository.findByCategoryOrderByPublishedAtDesc(category, pageable)
                .map(this::toResponse);
    }

    // 3. Tìm kiếm theo tiêu đề + phân trang + sắp xếp mới nhất
    public Page<ArticleResponse> searchArticlesByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return articleRepository.searchByTitle(keyword, pageable)
                .map(this::toResponse);
    }

    // 4. Chi tiết bài viết (giữ nguyên)
    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found with id: " + id));
        return toResponse(article);
    }

    /* ====================== ADMIN METHODS (cần auth) ====================== */

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

    /* ====================== PRIVATE HELPER ====================== */

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