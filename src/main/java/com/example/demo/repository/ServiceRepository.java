package com.example.demo.repository;

import com.example.demo.entity.Service;
import com.example.demo.entity.Service.ServiceCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {
    List<Service> findAllByIsActiveTrue();
    List<Service> findAllByServiceIdInAndIsActiveTrue(List<String> serviceIds);
    Set<Service> findAllByServiceIdIn(List<String> serviceIds);
    Page<Service> findByIsActiveTrue(Pageable pageable);
    Page<Service> findByCategoryAndIsActiveTrue(ServiceCategory category, Pageable pageable);
}
