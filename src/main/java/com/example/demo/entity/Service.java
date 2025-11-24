package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "SERVICES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @Column(name = "service_id", length = 36)
    private String serviceId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ServiceCategory category;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    @Column(name = "photo_url")
    private String photoUrl;
    public enum ServiceCategory {
        Consultation("Khám bệnh"),
        Test("Thăm dò"),
        Procedure("Thủ thuật");

        private final String displayName;

        ServiceCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}