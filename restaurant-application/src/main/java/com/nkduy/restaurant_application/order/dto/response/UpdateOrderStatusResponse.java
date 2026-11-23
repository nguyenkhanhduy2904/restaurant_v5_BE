package com.nkduy.restaurant_application.order.dto.response;

public record UpdateOrderStatusResponse(
        String orderId,
        String status
) {
}
