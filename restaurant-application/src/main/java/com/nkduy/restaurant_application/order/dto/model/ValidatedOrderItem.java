package com.nkduy.restaurant_application.order.dto.model;

import com.nkduy.restaurant_application.product.entity.Product;
import com.nkduy.restaurant_application.voucher.service.pricing.model.CartLine;

import java.math.BigDecimal;

public record ValidatedOrderItem(
        Product product,

        CartLine cartLine,
        String note

) {
    public OrderItemDto toOrderItemDto() {
        return new OrderItemDto(

                product().getId(),
                product().getName(),
                cartLine().quantity(),
                cartLine().unitPrice(),
                note()

        );
    }
}
