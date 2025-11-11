// File: src/main/java/com/example/demo/service/ReviewService.java
package com.example.demo.service;

import com.example.demo.dto.ReviewSummary;
import com.example.demo.entity.Review;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Review submitReview(Review review) {
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5.");
        }
        return reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Double getAverageRating() {
        return getAllReviews().stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // SỬA: Dùng POJO, không lỗi
    public ReviewSummary getReviewSummary() {
        List<Review> reviews = getAllReviews();
        return new ReviewSummary(reviews, getAverageRating(), reviews.size());
    }
}