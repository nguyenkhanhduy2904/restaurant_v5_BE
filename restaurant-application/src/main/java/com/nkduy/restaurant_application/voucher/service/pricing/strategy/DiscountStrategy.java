package com.nkduy.restaurant_application.voucher.service.pricing.strategy;

import com.nkduy.restaurant_application.voucher.service.pricing.model.Cart;
import com.nkduy.restaurant_application.voucher.service.pricing.model.DiscountResult;
import com.nkduy.restaurant_application.voucher.service.pricing.model.VoucherType;

import java.util.Map;

public interface DiscountStrategy {
    VoucherType type();

    void validateParams(Map<String, Object> params);

    DiscountResult calculate(Cart cart, Map<String, Object> params);
}
