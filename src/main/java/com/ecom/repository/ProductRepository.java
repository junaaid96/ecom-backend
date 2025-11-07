package com.ecom.repository;

import com.ecom.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Product> findByCategorySlug(String categorySlug, Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN p.category c WHERE LOWER(c.slug) = LOWER(:categorySlug) AND p.id != :excludeId ORDER BY p.createdAt DESC")
    List<Product> findRelatedProductsByCategorySlug(@Param("categorySlug") String categorySlug, @Param("excludeId") Long excludeId, Pageable pageable);
}

