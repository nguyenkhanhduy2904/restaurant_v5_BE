package com.nkduy.restaurant_application.product.dto.response;

import com.nkduy.restaurant_application.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.Set;

public record ProductSummaryResponse(
        String id,
        String name,
        BigDecimal unitPrice,
        ProductStatus status,
        Set<String> tagIds,
        String thumbnailImageUrl
)
{
}
