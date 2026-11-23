package com.nkduy.restaurant_application.order.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank String status
) {
}
