package com.nkduy.restaurant_application.voucher.service.pricing.model;

import java.math.BigDecimal;

public record UserContext(
        String userId,
        boolean isNewCustomer,
        BigDecimal totalSpend,
        String tier
) {
}
