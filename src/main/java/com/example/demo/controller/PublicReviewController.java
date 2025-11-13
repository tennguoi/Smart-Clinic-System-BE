package com.example.demo.controller;

import com.example.demo.dto.ReviewRequest;
import com.example.demo.dto.ReviewResponse;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/reviews")
public class PublicReviewController {
    
    private final ReviewService reviewService;

    public PublicReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Submit review (không cần đăng nhập)
    @PostMapping
    public ResponseEntity<ReviewResponse> submitReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Lấy tất cả reviews
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // Lấy review theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    // Lấy reviews theo rating
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRating(@PathVariable Integer rating) {
        return ResponseEntity.ok(reviewService.getReviewsByRating(rating));
    }

    // Lấy tóm tắt reviews (average rating + latest reviews)
    @GetMapping("/summary")
    public ResponseEntity<java.util.Map<String, Object>> getReviewSummary() {
        return ResponseEntity.ok(reviewService.getReviewSummary());
    }
}
