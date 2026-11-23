package com.nkduy.restaurant_application.voucher.service.pricing;


import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.service.pricing.helper.DiscountAllocator;
import com.nkduy.restaurant_application.voucher.service.pricing.model.*;
import com.nkduy.restaurant_application.voucher.service.pricing.rule.VoucherValidator;
import com.nkduy.restaurant_application.voucher.service.pricing.strategy.DiscountStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Service

public class PricingService {

    private final Map<VoucherType, DiscountStrategy> strategies = new EnumMap<>(VoucherType.class);
    private final VoucherValidator voucherValidator;
    private final Clock clock;


    public PricingService(List<DiscountStrategy> strategyList, VoucherValidator voucherValidator, Clock clock) {
        this.voucherValidator = voucherValidator;
        this.clock = clock;
        for(DiscountStrategy strategy : strategyList){//check duplicate strategy
            VoucherType type = strategy.type();

            if(strategies.containsKey(type)){
                throw new IllegalStateException("Duplicate strategy for type: " + type);
            }
            strategies.put(type, strategy);
        }

        for(VoucherType type : VoucherType.values()){//check for strategy enum that dont have strategy class
            if(!strategies.containsKey(type)){
                throw new IllegalStateException("No strategy found for type: " + type);
            }

        }
        //2 loop will fail at start up, not when customer use the voucher
    }

    public PriceBreakDown calculate(Cart cart, OrderVoucher voucher, UserContext userContext){

        if(voucher == null){//no voucher
            return PriceBreakDown.none(cart);
        }

        //reject
        String reason = voucherValidator.validate(voucher, cart, userContext, clock.instant().atZone(clock.getZone()));
        if(reason != null){
            return PriceBreakDown.rejected(cart, reason);
        }

        DiscountResult rawResult = strategies.get(voucher.getDiscountType()).calculate(cart, voucher.getParam());// make sure discount type use here and voucher type are the same type, it might be String vs Enum


        //final price, safe, apply cap, clamp
        Map<String, BigDecimal> result = clamp(rawResult, cart);
        result = applyCap(result, voucher.getMaxDiscount());
        return assemble(cart, result);

    }

    private Map<String, BigDecimal> clamp(DiscountResult rawResult, Cart cart){
        Map<String, BigDecimal> temp = new LinkedHashMap<>();

        for(CartLine line : cart.lines()){
            BigDecimal discount = rawResult.discountPerLine().getOrDefault(line.id(), BigDecimal.ZERO).min(line.total());// strategy didnt mention, set it as 0, also set min as which ever smaller, total or discount
            if(discount.signum() > 0){

                temp.put(line.id(), discount);//only put the line that has discount
            }
        }

        BigDecimal deliveryDiscount = rawResult.deliveryFee().min(cart.deliveryFee());
        if (deliveryDiscount.signum() > 0) {
            temp.put("DELIVERY", deliveryDiscount);
        }
        return temp;
    }


    private Map<String, BigDecimal> applyCap(Map<String, BigDecimal> parts, BigDecimal maxDiscount) {
        if (maxDiscount == null || sum(parts).compareTo(maxDiscount) <= 0) {//if max discount is null, or sum of all discount is less than max discount, return the raw result
            return parts;
        }
        // amount to split = the cap, weights = the current discounts
        return DiscountAllocator.split(maxDiscount, parts);// if sum of all discount is greater than max discount, use max discount as the sum, so it wouldnt exceed max discount
    }

    private PriceBreakDown assemble(Cart cart, Map<String, BigDecimal> t){
        BigDecimal deliveryDiscount = t.getOrDefault("DELIVERY", BigDecimal.ZERO);

        Map<String, BigDecimal> perLine = new LinkedHashMap<>(t);
        perLine.remove("DELIVERY"); //remove delivery discount from the map


        BigDecimal totalDiscount = sum(perLine).add(deliveryDiscount);
        BigDecimal total = cart.cartTotal().add(cart.deliveryFee()).subtract(totalDiscount);//total = cart total + delivery fee - total discount

        return new PriceBreakDown(cart.cartTotal(), cart.deliveryFee(), perLine, deliveryDiscount, total, null);

    }




    private static BigDecimal sum(Map<String, BigDecimal> m) {
        return m.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
