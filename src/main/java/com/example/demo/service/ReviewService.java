// File: src/main/java/com/example/demo/service/ReviewService.java

package com.example.demo.service;

import com.example.demo.entity.Review; // << Sửa package
import com.example.demo.repository.ReviewRepository; // << Sửa package
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Gửi đánh giá mới.
     */
    @Transactional
    public Review submitReview(Review review) {
        // Kiểm tra cơ bản
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải nằm trong khoảng từ 1 đến 5.");
        }
        if (review.getClinicId() == null) {
            throw new IllegalArgumentException("Phải chỉ định phòng khám/bác sĩ được đánh giá.");
        }
        return reviewRepository.save(review);
    }

    /**
     * Lấy danh sách đánh giá theo ID phòng khám/bác sĩ (Truy cập công khai).
     */
    public List<Review> getReviewsByClinicId(Long clinicId) {
        return reviewRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);
    }

    /**
     * Tính điểm trung bình của một phòng khám.
     */
    public Double getAverageRating(Long clinicId) {
        List<Review> reviews = reviewRepository.findByClinicIdOrderByCreatedAtDesc(clinicId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}