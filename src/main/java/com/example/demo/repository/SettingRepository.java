package com.example.demo.repository;

import com.example.demo.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettingRepository extends JpaRepository<Setting, UUID> {
    
    Optional<Setting> findByUserId(UUID userId);
    
    boolean existsByUserId(UUID userId);
}
