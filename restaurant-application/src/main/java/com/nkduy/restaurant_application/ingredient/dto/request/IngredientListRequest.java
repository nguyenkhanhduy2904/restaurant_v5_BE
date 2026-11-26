package com.nkduy.restaurant_application.ingredient.dto.request;

import java.util.List;

public record IngredientListRequest(
        List<String> ingredientIds
) {
}
