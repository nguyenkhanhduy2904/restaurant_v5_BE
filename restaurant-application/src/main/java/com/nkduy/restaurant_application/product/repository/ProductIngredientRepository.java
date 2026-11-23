package com.nkduy.restaurant_application.product.repository;

import com.nkduy.restaurant_application.product.entity.ProductIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface ProductIngredientRepository extends JpaRepository<ProductIngredient, String> {
    List<ProductIngredient> findAllByProductIdAndIngredientIdIn(String id, Set<String> strings);

    List<ProductIngredient> findAllByProductId(String id);
}
