package com.nkduy.restaurant_application.product.dto.response;

import com.nkduy.restaurant_application.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public record ProductResponse(
        String id,
        String name,
        BigDecimal unitPrice,
        String description,
        ProductStatus status,
        Set<String> tagIds,
        Map<Integer, String> images,
        Map<String, Double> ingredientMap//ingredientId -> qty
) {
}
