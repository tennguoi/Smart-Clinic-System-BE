// File: src/main/java/com/example/demo/entity/Review.java

package com.example.demo.entity;

import jakarta.persistence.*; // Hoặc javax.persistence.* nếu bạn dùng Spring Boot cũ
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long clinicId; // ID của phòng khám/bác sĩ được đánh giá

    @Min(1) @Max(5)
    private Integer rating; // Điểm đánh giá từ 1 đến 5

    @Lob
    private String comment;

    private String reviewerName;
    private String reviewerEmail;

    @CreationTimestamp
    private LocalDateTime createdAt; // Thời gian tạo

    // Constructors
    public Review() {
    }

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClinicId() { return clinicId; }
    public void setClinicId(Long clinicId) { this.clinicId = clinicId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public String getReviewerEmail() { return reviewerEmail; }
    public void setReviewerEmail(String reviewerEmail) { this.reviewerEmail = reviewerEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}