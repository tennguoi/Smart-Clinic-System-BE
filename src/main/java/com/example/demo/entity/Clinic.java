package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "clinic_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clinic {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "clinic_id", updatable = false, nullable = false)
    private UUID clinicId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "phone", length = 20)
    private String phone;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "website", length = 255)
    private String website;
    
    @Column(name = "logo_url", length = 500)
    private String logoUrl;
    
    @Column(name = "morning_start_time")
    private LocalTime morningStartTime;
    
    @Column(name = "morning_end_time")
    private LocalTime morningEndTime;
    
    @Column(name = "afternoon_start_time")
    private LocalTime afternoonStartTime;
    
    @Column(name = "afternoon_end_time")
    private LocalTime afternoonEndTime;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Đảm bảo chỉ có 1 bản ghi duy nhất
    @PrePersist
    public void prePersist() {
        if (clinicId == null) {
            clinicId = UUID.randomUUID();
        }
    }
}

