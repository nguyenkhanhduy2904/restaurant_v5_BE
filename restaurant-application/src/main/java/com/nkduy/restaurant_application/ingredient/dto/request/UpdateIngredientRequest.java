package com.nkduy.restaurant_application.ingredient.dto.request;

import com.nkduy.restaurant_application.ingredient.entity.IngredientStatus;

import jakarta.validation.constraints.Size;

public record UpdateIngredientRequest(
        @Size(max = 255) String name,
        @Size(max = 255) String description,
        @Size(max = 50) String unit,
        IngredientStatus status

) {
}
