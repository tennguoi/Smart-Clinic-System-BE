// src/main/java/com/example/demo/repository/ReviewRepository.java

package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRating(Integer rating);

    @Query("SELECT r FROM Review r " +
            "WHERE LOWER(r.reviewerName) LIKE LOWER(:term) " +
            "   OR LOWER(r.reviewerEmail) LIKE LOWER(:term)")
    Page<Review> search(@Param("term") String term, Pageable pageable);
}