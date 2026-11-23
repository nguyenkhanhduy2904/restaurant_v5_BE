package com.nkduy.restaurant_application.voucher.service.pricing.model;

import java.math.BigDecimal;
import java.util.Map;

public record PriceBreakDown(
        BigDecimal total,
        BigDecimal deliveryFee,
        Map<String, BigDecimal> discountPerLine,
        BigDecimal deliveryDiscount,
        BigDecimal finalTotal,
        String rejectReason
){
    public BigDecimal itemTotalDiscount(){
        return discountPerLine.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal totalDiscount(){
        return deliveryDiscount.add(itemTotalDiscount());
    }
    public boolean isRejected(){
        return rejectReason != null;
    }

    public static PriceBreakDown none(Cart cart){
        return new PriceBreakDown(cart.cartTotal(), cart.deliveryFee(), Map.of(), BigDecimal.ZERO, cart.cartTotal(), null);
    }

    public static PriceBreakDown rejected(Cart cart,String reason){
        return new PriceBreakDown(cart.cartTotal(), cart.deliveryFee(), Map.of(), BigDecimal.ZERO, cart.cartTotal(), reason);
    }
}
