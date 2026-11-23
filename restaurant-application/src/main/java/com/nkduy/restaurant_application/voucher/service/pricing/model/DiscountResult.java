package com.nkduy.restaurant_application.voucher.service.pricing.model;

import java.math.BigDecimal;
import java.util.Map;

public record DiscountResult(
        Map<String, BigDecimal> discountPerLine,
        BigDecimal deliveryFee

) {
    public static DiscountResult none(){
        return new DiscountResult(Map.of(), BigDecimal.ZERO );
    }
    public static DiscountResult delivery(BigDecimal d){
        return new DiscountResult(Map.of(), d);
    }
    public static DiscountResult line(Map<String, BigDecimal> m){
        return new DiscountResult(m, BigDecimal.ZERO);
    }
}
