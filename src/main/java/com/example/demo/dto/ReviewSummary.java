// File: src/main/java/com/example/demo/dto/ReviewSummary.java
package com.example.demo.dto;

import com.example.demo.entity.Review;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ReviewSummary {

    @JsonProperty("reviews")
    private final List<Review> reviews;

    @JsonProperty("averageRating")
    private final Double averageRating;

    @JsonProperty("totalReviews")
    private final int totalReviews;

    // Constructor
    public ReviewSummary(List<Review> reviews, Double averageRating, int totalReviews) {
        this.reviews = reviews;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
    }

    // Getters
    public List<Review> getReviews() { return reviews; }
    public Double getAverageRating() { return averageRating; }
    public int getTotalReviews() { return totalReviews; }
}