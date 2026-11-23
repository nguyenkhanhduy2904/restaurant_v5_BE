package com.nkduy.restaurant_application.voucher.service.pricing.model;

import java.math.BigDecimal;
import java.util.List;

public record Cart(
        List<CartLine> lines,
        String orderType,
        String branchId,
        BigDecimal deliveryFee
) {
    public BigDecimal cartTotal() {
        return lines.stream().map(CartLine::total).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
