package com.nkduy.restaurant_application.ingredient.repository;

import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, String> {
    Page<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);

//    Optional<Ingredient> findAllById(List<String> ids);
}
