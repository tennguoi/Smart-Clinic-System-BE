package com.example.demo.repository;

import com.example.demo.entity.Service;
import com.example.demo.entity.Service.ServiceCategory;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    @Query("""
    SELECT s FROM Service s
    WHERE (:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:category IS NULL OR s.category = :category)
      AND (:isActive IS NULL OR s.isActive = :isActive)
      AND (:minPrice IS NULL OR s.price >= :minPrice)
      AND (:maxPrice IS NULL OR s.price <= :maxPrice)
""")
    Page<Service> searchServices(
            @Param("name") String name,
            @Param("category") com.example.demo.entity.Service.ServiceCategory category,
            @Param("isActive") Boolean isActive,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );
}
