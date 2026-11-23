package com.nkduy.restaurant_application.product.dto.request;

import com.nkduy.restaurant_application.product.entity.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public record UpdateProductRequest(

        @Size(max = 255)
        String name,


        @DecimalMin(value = "0")
        @Digits(integer = 12, fraction = 0)
        BigDecimal unitPrice,

        @Size(max = 255)
        String description,


        Set<@NotBlank String> tagIds,

        Map<@NotBlank String, @NotNull @Positive Double> ingredientMap,

        Map<@NotNull @Positive Integer, @NotBlank @Size(max = 255) String> images,

        Boolean availableAtEveryBranch,

        Set<@NotBlank String> excludedBranchIds,

        ProductStatus status
) {
}
