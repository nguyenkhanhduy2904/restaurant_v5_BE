package com.nkduy.restaurant_application.product.dto.request;

import com.nkduy.restaurant_application.product.entity.TagStatus;
import jakarta.validation.constraints.Size;

public record UpdateTagRequest(
        @Size(max = 50) String name,
        TagStatus status
) {
}
