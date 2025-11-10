package com.example.demo.repository;

import com.example.demo.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, String> {

    /**
     * Lấy tất cả dịch vụ đang hoạt động với phân trang
     * @param pageable Thông tin phân trang
     * @return Page<Service>
     */
    Page<Service> findByIsActiveTrue(Pageable pageable);
}