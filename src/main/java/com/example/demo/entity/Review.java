// src/main/java/com/example/demo/entity/Review.java
package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
        indexes = {
                @Index(name = "idx_review_rating", columnList = "rating"),
                @Index(name = "idx_review_email", columnList = "reviewerEmail"),
                @Index(name = "idx_review_created", columnList = "createdAt")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating tối thiểu là 1")
    @Max(value = 5, message = "Rating tối đa là 5")
    @Column(nullable = false)
    private Integer rating;

    @Lob
    @Basic(fetch = FetchType.LAZY) // tiết kiệm bộ nhớ khi không cần comment
    @Size(max = 2000, message = "Bình luận không quá 2000 ký tự")
    private String comment;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 100, message = "Tên từ 2-100 ký tự")
    @Column(nullable = false, length = 100)
    private String reviewerName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String reviewerEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Optional: nếu bạn muốn ẩn/hiện review trên web
    @Builder.Default
    @Column(nullable = false)
    private boolean published = true;

    // Optional: cho admin đánh dấu spam
    @Builder.Default
    @Column(nullable = false)
    private boolean spam = false;
}