// src/main/java/com/example/demo/service/ReviewService.java

package com.example.demo.service;

import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.ReviewResponse;
import com.example.demo.entity.Review;
import com.example.demo.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // ============================= PUBLIC API =============================

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        validateRating(request.rating());
        Review review = mapToEntity(request);
        return toResponse(reviewRepository.save(review));
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, Object> getReviewSummary() {
        List<Review> all = reviewRepository.findAll();
        if (all.isEmpty()) {
            return Map.of("reviews", Collections.emptyList(), "averageRating", 0.0, "totalReviews", 0);
        }
        double avg = all.stream().mapToInt(Review::getRating).average().orElse(0.0);
        List<ReviewResponse> latest = all.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(20)
                .map(this::toResponse)
                .toList();
        return Map.of("reviews", latest, "averageRating", Math.round(avg * 10.0) / 10.0, "totalReviews", all.size());
    }

    public ReviewResponse getReviewById(Long id) {
        return toResponse(findEntityById(id));
    }

    public List<ReviewResponse> getReviewsByRating(Integer rating) {
        return reviewRepository.findByRating(rating).stream()
                .map(this::toResponse)
                .toList();
    }

    // ============================= ADMIN API =============================

    public Page<ReviewResponse> findAllPagedResponse(Pageable pageable, String search) {
        Page<Review> page;
        if (search == null || search.trim().isEmpty()) {
            page = reviewRepository.findAll(pageable);
        } else {
            String term = "%" + search.toLowerCase() + "%";
            page = reviewRepository.search(term, pageable);
        }
        return page.map(this::toResponse);
    }

    public ReviewResponse getReviewResponseById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public ReviewResponse createByAdmin(ReviewRequest request) {
        validateRating(request.rating());
        Review review = mapToEntity(request);
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public ReviewResponse updateByAdmin(Long id, ReviewRequest request) {
        validateRating(request.rating());
        Review review = findEntityById(id);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setReviewerName(request.reviewerName());
        review.setReviewerEmail(request.reviewerEmail());
        return toResponse(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }

    // ============================= PRIVATE =============================

    private Review findEntityById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private Review mapToEntity(ReviewRequest req) {
        return Review.builder()
                .rating(req.rating())
                .comment(req.comment())
                .reviewerName(req.reviewerName())
                .reviewerEmail(req.reviewerEmail())
                .build();
    }

    private ReviewResponse toResponse(Review entity) {
        return new ReviewResponse(
                entity.getId(),
                entity.getRating(),
                entity.getComment(),
                entity.getReviewerName(),
                entity.getReviewerEmail(),
                entity.getCreatedAt()
        );
    }
}