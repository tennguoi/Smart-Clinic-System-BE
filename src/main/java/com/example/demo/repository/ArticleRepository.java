package com.example.demo.repository;

import com.example.demo.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findAllByOrderByPublishedAtDesc(Pageable pageable);

    Page<Article> findByCategoryOrderByPublishedAtDesc(String category, Pageable pageable);

    @Query("""
        SELECT a FROM Article a
        WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
        ORDER BY a.publishedAt DESC
    """)
    Page<Article> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
}
