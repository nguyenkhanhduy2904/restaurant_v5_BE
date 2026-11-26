package com.nkduy.restaurant_application.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record PreviewOrderRequest(
        @NotBlank(message = "Branch ID is required")
        String branchId,

//        @NotBlank(message = "User ID is required")
//        String userId,

        @NotBlank(message = "Delivery address is required")
        @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
        BigDecimal longitude,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
        BigDecimal latitude,


        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid CreateOrderItemRequest> item,

        String orderVoucherId,
        String deliveryVoucherId


) {
}