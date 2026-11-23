package com.nkduy.restaurant_application.order.dto.model;

import java.math.BigDecimal;

public record OrderItemDto(

        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        String note
) {
}
