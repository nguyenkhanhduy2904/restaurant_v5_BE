package com.nkduy.restaurant_application.ingredient.dto.model;

import com.nkduy.restaurant_application.ingredient.entity.IngredientStatus;

public record IngredientDto(
        String id,
        String name,
        String description,
        String unit,
        IngredientStatus status
) {
}
