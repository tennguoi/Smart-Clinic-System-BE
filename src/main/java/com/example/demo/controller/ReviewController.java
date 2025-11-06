// src/main/java/com/example/demo/controller/ReviewController.java
package com.example.demo.controller;

import com.example.demo.entity.Review;
import com.example.demo.repository.ReviewRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
public class ReviewController {

    private final ReviewRepository reviewRepo;

    public ReviewController(ReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReviews(
            @RequestParam(defaultValue = "Tất cả") String service,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int limit) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Specification<Review> spec = approvedOnly();

        if (!"Tất cả".equals(service)) spec = spec.and(byService(service));
        if (!search.isBlank()) spec = spec.and(bySearch(search));

        Page<Review> reviewPage = reviewRepo.findAll(spec, pageable);
        List<Map<String, Object>> formatted = reviewPage.getContent().stream()
                .map(this::formatReview)
                .toList();

        Map<String, Object> stats = calculateStats();
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", formatted);
        response.put("pagination", Map.of(
                "page", page, "limit", limit,
                "total", reviewPage.getTotalElements(),
                "totalPages", reviewPage.getTotalPages()
        ));
        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> formatReview(Review r) {
        String fullName = r.getUser().getFullName();
        String maskedName = "K*** H.";
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+");
            String first = parts[0];
            String last = parts.length > 1 ? parts[parts.length - 1] : "";
            maskedName = last.charAt(0) + "*** " + first.charAt(0) + ".";
        }
        return Map.of(
                "id", r.getId(),
                "name", maskedName,
                "content", r.getContent(),
                "rating", r.getRating(),
                "service", Optional.ofNullable(r.getService()).orElse("Chung"),
                "date", r.getCreatedAt().toLocalDate().toString(),
                "verified", r.getVerified()
        );
    }

    private Map<String, Object> calculateStats() {
        List<Review> all = reviewRepo.findByApprovedTrue();
        if (all.isEmpty()) return Map.of("average", "0.0", "total", 0, "recommend", 0);
        double avg = all.stream().mapToInt(Review::getRating).average().orElse(0.0);
        long recommendCount = all.stream().filter(r -> r.getRating() >= 4).count();
        return Map.of(
                "average", String.format("%.1f", avg),
                "total", all.size(),
                "recommend", Math.round((double) recommendCount / all.size() * 100)
        );
    }

    private Specification<Review> approvedOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("approved"));
    }
    private Specification<Review> byService(String service) {
        return (root, query, cb) -> cb.equal(root.get("service"), service);
    }
    private Specification<Review> bySearch(String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        return (root, query, cb) -> {
            Predicate content = cb.like(cb.lower(root.get("content")), pattern);
            Predicate name = cb.like(cb.lower(root.get("user").get("fullName")), pattern);
            return cb.or(content, name);
        };
    }
}