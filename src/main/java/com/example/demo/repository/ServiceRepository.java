package com.example.demo.repository;

import com.example.demo.entity.Service;
import com.example.demo.entity.Service.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    Page<Service> findByIsActiveTrue(Pageable pageable);
    Page<Service> findByCategoryAndIsActiveTrue(ServiceCategory category, Pageable pageable);
}
