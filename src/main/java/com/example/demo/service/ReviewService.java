// File: src/main/java/com/example/demo/service/ReviewService.java
package com.example.demo.service;

import com.example.demo.dto.ReviewSummary;
import com.example.demo.entity.Review;
import com.example.demo.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.ReviewResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // Public methods - User không cần đăng nhập có thể submit review
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        // Validate rating
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = new Review();
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setReviewerName(request.reviewerName());
        review.setReviewerEmail(request.reviewerEmail());
        
        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public java.util.Map<String, Object> getReviewSummary() {
        List<Review> allReviews = reviewRepository.findAll();
        
        if (allReviews.isEmpty()) {
            return java.util.Map.of(
                "reviews", java.util.Collections.emptyList(),
                "averageRating", 0.0,
                "totalReviews", 0
            );
        }
        
        double avgRating = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        
        List<ReviewResponse> reviewResponses = allReviews.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(20)
                .map(this::toResponse)
                .collect(Collectors.toList());
        
        return java.util.Map.of(
            "reviews", reviewResponses,
            "averageRating", Math.round(avgRating * 10.0) / 10.0,
            "totalReviews", allReviews.size()
        );
    }

    public ReviewResponse getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));
        return toResponse(review);
    }

    public List<ReviewResponse> getReviewsByRating(Integer rating) {
        return reviewRepository.findByRating(rating).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Admin method - Xóa review nếu cần
    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRating(),
                review.getComment(),
                review.getReviewerName(),
                review.getReviewerEmail(),
                review.getCreatedAt()
        );
    }
}
