// File: src/main/java/com/example/demo/controller/ReviewController.java
package com.example.demo.controller;

import com.example.demo.dto.ReviewSummary;
import com.example.demo.entity.Review;
import com.example.demo.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // POST: Gửi đánh giá mới (toàn cục)
    @PostMapping
    public ResponseEntity<Review> submitReview(@Valid @RequestBody Review review) {
        try {
            Review saved = reviewService.submitReview(review);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // GET: Lấy tất cả đánh giá (toàn cục)
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    // GET: Tính điểm trung bình toàn hệ thống
    @GetMapping("/average-rating")
    public ResponseEntity<Double> getAverageRating() {
        Double avg = reviewService.getAverageRating();
        return ResponseEntity.ok(avg);
    }

    // GET: Tóm tắt toàn bộ (tối ưu cho FE)
    // File: src/main/java/com/example/demo/controller/ReviewController.java
    @GetMapping("/summary")
    public ResponseEntity<ReviewSummary> getReviewSummary() {
        ReviewSummary summary = reviewService.getReviewSummary();
        return ResponseEntity.ok(summary);
    }
}