package com.nkduy.restaurant_application.ingredient.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIngredientRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 255) String description,
        @NotBlank @Size(max = 50) String unit
){
}
