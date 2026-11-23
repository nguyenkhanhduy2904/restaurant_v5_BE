package com.nkduy.restaurant_application.product.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public record CreateProductRequest(
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @DecimalMin(value = "0")
        @Digits(integer = 12, fraction = 0)
        BigDecimal unitPrice,

        @Size(max = 255)
        String description,

        @NotEmpty(message = "At least one tag is required")
        Set<@NotBlank String> tagIds,

        Map<@NotBlank String, @NotNull @Positive Double> ingredientMap,

        Map<@NotNull Integer, @NotBlank @Size(max = 255) String> images

//        @NotNull boolean availableAtEveryBranch,
//
//        Set<@NotBlank String> excludedBranchIds

) {
}
