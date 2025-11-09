// File: src/main/java/com/example/demo/controller/ReviewController.java

package com.example.demo.controller;

import com.example.demo.entity.Review; // << Sửa package
import com.example.demo.service.ReviewService; // << Sửa package
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * POST /api/reviews : Gửi đánh giá mới (Truy cập công khai).
     */
    @PostMapping
    public ResponseEntity<Review> submitReview(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.submitReview(review);
            return new ResponseEntity<>(savedReview, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * GET /api/reviews/clinic/{clinicId} : Lấy danh sách đánh giá (Truy cập công khai).
     */
    @GetMapping("/clinic/{clinicId}")
    public ResponseEntity<List<Review>> getReviewsByClinicId(@PathVariable Long clinicId) {
        List<Review> reviews = reviewService.getReviewsByClinicId(clinicId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * GET /api/reviews/clinic/{clinicId}/average-rating : Lấy điểm trung bình (Truy cập công khai).
     */
    @GetMapping("/clinic/{clinicId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long clinicId) {
        Double averageRating = reviewService.getAverageRating(clinicId);
        return ResponseEntity.ok(averageRating);
    }
}