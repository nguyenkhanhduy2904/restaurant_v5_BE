package com.nkduy.restaurant_application.product.repository;

import com.nkduy.restaurant_application.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, String> {
    Optional<ProductImage> findFirstByProductIdOrderByDisplayOrderAsc(
            String productId
    );

    List<ProductImage> findAllByProductIdOrderByDisplayOrderAsc(
            String productId
    );
}
