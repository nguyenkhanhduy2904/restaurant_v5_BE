package com.nkduy.restaurant_application.voucher.service.pricing.model;

import com.nkduy.restaurant_application.product.entity.Tag;

import java.math.BigDecimal;
import java.util.List;

public record CartLine(
        String id,
        String productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        List<String> tagId
) {
    public BigDecimal total() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
