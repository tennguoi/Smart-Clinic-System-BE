package com.example.demo.repository;

import com.example.demo.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 1. Lấy tất cả bài viết + phân trang + sắp xếp mới nhất trước
    Page<Article> findAllByOrderByPublishedAtDesc(Pageable pageable);

    // 2. Lấy theo danh mục + phân trang + sắp xếp mới nhất
    Page<Article> findByCategoryOrderByPublishedAtDesc(String category, Pageable pageable);

    // 3. Tìm kiếm theo tiêu đề (không phân biệt hoa thường) + phân trang
    @Query("SELECT a FROM Article a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) ORDER BY a.publishedAt DESC")
    Page<Article> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // Nếu vẫn muốn giữ method cũ trả List (dùng cho admin hoặc nơi khác)
    // List<Article> findByCategory(String category);
    // List<Article> findByTitleContainingIgnoreCase(String title);
}