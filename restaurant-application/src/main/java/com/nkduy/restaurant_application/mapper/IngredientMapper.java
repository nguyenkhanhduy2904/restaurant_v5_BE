package com.nkduy.restaurant_application.mapper;

import com.nkduy.restaurant_application.ingredient.dto.model.IngredientDto;
import com.nkduy.restaurant_application.ingredient.entity.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientMapper {
    public IngredientDto toIngredientDto(Ingredient ingredient){
        return new IngredientDto(
                ingredient.getId(),
                ingredient.getName(),
                ingredient.getDescription(),
                ingredient.getUnit(),
                ingredient.getStatus()
        );
    }
}
