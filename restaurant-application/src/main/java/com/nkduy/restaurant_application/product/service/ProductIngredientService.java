package com.nkduy.restaurant_application.product.service;

import com.nkduy.restaurant_application.ingredient.repository.IngredientRepository;
import com.nkduy.restaurant_application.ingredient.service.IngredientService;
import com.nkduy.restaurant_application.product.entity.Product;
import com.nkduy.restaurant_application.product.entity.ProductIngredient;
import com.nkduy.restaurant_application.product.repository.ProductIngredientRepository;
import com.nkduy.restaurant_application.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductIngredientService {
    private final ProductRepository productRepository;
    private final IngredientRepository ingredientRepository;
    private final ProductIngredientRepository productIngredientRepository;




    @Transactional
    public void syncProductIngredients(
            Product product,
            Map<String, Double> ingredientMap
    ) {
        List<ProductIngredient> existedRows =
                productIngredientRepository
                        .findAllByProductIdAndIngredientIdIn(
                                product.getId(),
                                ingredientMap.keySet()
                        );

        Set<String> existedIngredientIds = new HashSet<>();

        // Update existing rows
        for (ProductIngredient row : existedRows) {
            String ingredientId = row.getIngredient().getId();

            existedIngredientIds.add(ingredientId);

            double qty = ingredientMap.get(ingredientId);
            row.setQuantity(BigDecimal.valueOf(qty));
        }

        List<ProductIngredient> newRows = new ArrayList<>();

        // Create missing rows
        for (Map.Entry<String, Double> entry : ingredientMap.entrySet()) {
            String ingredientId = entry.getKey();

            if (!existedIngredientIds.contains(ingredientId)) {
                ProductIngredient newRow = ProductIngredient.builder()
                        .product(product)
                        .ingredient(ingredientRepository.getReferenceById(ingredientId))
                        .quantity(BigDecimal.valueOf(entry.getValue()))
                        .build();

                newRows.add(newRow);
            }
        }

        productIngredientRepository.saveAll(existedRows);
        productIngredientRepository.saveAll(newRows);
    }


    public Map<String, Double> getProductIngredients(Product product){
        List<ProductIngredient> existedRow = productIngredientRepository.findAllByProductId(product.getId());


        Map<String, Double> ingredientMap = new HashMap<>();
        for (ProductIngredient row : existedRow) {
            ingredientMap.put(row.getIngredient().getId(), row.getQuantity().doubleValue());
        }
        return ingredientMap;
    }


}
