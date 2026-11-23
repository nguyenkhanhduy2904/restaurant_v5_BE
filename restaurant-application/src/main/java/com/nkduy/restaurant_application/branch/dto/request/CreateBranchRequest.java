package com.nkduy.restaurant_application.branch.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record CreateBranchRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
        BigDecimal longitude,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
        BigDecimal latitude,

        @NotNull
        boolean haveEveryProduct,


        Set<String> excludedProductIds
) {
}
