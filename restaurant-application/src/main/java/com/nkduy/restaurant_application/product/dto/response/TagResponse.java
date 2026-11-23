package com.nkduy.restaurant_application.product.dto.response;

import com.nkduy.restaurant_application.product.entity.TagStatus;

public record TagResponse(
        String id,
        String name,
        TagStatus status
) {
}
