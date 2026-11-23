package com.nkduy.restaurant_application.order.dto.response;

import com.nkduy.restaurant_application.order.dto.model.OrderDto;

public record CreateOrderResponse(
        OrderDto orderDto
) {
}
