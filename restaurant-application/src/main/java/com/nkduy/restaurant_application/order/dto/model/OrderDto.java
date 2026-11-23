package com.nkduy.restaurant_application.order.dto.model;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        String orderId,
        String userId,
        String branchId,

        List<OrderItemDto> items,
        int totalItemCount,
        BigDecimal totalPrice,
        BigDecimal deliveryDistance,
        BigDecimal deliveryFee,
        BigDecimal orderDiscount,
        BigDecimal deliveryDiscount,
        BigDecimal finalPrice,
        String address,
        String contactPhone,
        String contactName,
        String status,

        String orderVoucherId,
        String deliveryVoucherId
) {
}
