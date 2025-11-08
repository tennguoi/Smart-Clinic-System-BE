package com.example.demo.repository;

import com.example.demo.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {

    /**
     * Lấy tất cả dịch vụ đang hoạt động
     * @return Danh sách dịch vụ active
     */
    List<Service> findByIsActiveTrue();
}