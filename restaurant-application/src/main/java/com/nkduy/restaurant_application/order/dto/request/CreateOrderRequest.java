package com.nkduy.restaurant_application.order.dto.request;

import java.math.BigDecimal;

public record CreateOrderRequest(
        PreviewOrderRequest previewOrderRequest,

        String contactPhone,
        String contactName,

        String paymentMethod,
        String paymentStatus,
        BigDecimal paymentAmount,
        String transactionRef
){
}
