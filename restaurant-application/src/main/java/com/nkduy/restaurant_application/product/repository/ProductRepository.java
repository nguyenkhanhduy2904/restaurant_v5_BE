package com.nkduy.restaurant_application.product.repository;

import com.nkduy.restaurant_application.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, String> {
    // OR: Match at least one selected tag
    @Query("""
        SELECT DISTINCT p
        FROM Product p
        LEFT JOIN p.tags t
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:tagIds IS NULL OR t.id IN :tagIds)
    """)
    Page<Product> findProductsMatchingAnyTag(
            @Param("name") String name,
            @Param("tagIds") Set<String> tagIds,
            Pageable pageable
    );

    // AND: Match every selected tag
    @Query("""
        SELECT p
        FROM Product p
        WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (
              :tagIds IS NULL OR
              (
                  SELECT COUNT(DISTINCT t.id)
                  FROM Product p2
                  JOIN p2.tags t
                  WHERE p2.id = p.id
                    AND t.id IN :tagIds
              ) = :tagCount
          )
    """)
    Page<Product> findProductsMatchingAllTags(
            @Param("name") String name,
            @Param("tagIds") Set<String> tagIds,
            @Param("tagCount") long tagCount,
            Pageable pageable
    );
}
