package com.nkduy.restaurant_application.voucher.service.pricing.strategy;

import com.nkduy.restaurant_application.voucher.service.pricing.helper.DiscountAllocator;
import com.nkduy.restaurant_application.voucher.service.pricing.model.Cart;
import com.nkduy.restaurant_application.voucher.service.pricing.model.CartLine;
import com.nkduy.restaurant_application.voucher.service.pricing.model.DiscountResult;
import com.nkduy.restaurant_application.voucher.service.pricing.model.VoucherType;
import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
@Component
public class PercentOffOrder extends AbstractStrategy<PercentOffOrder.Params> {

    public PercentOffOrder(ObjectMapper strictMapper, Validator validator) {
        super(strictMapper, validator, Params.class);
    }


    public record Params(
            @NotNull @DecimalMin("0.01") @DecimalMax("100")
            BigDecimal percentage
    ){}

    @Override
    protected DiscountResult compute(Cart cart, Params params) {
        Map<String, BigDecimal> pricePerLine = new LinkedHashMap<>();
        for(CartLine line : cart.lines()){
            pricePerLine.put(line.id(), line.total());
        }
        BigDecimal totalDiscount = percentOf(cart.cartTotal(),params.percentage);
        return DiscountResult.line(DiscountAllocator.split(totalDiscount, pricePerLine));
    }

    @Override
    public VoucherType type() {
        return VoucherType.PERCENTAGE_OFF_ORDER;
    }
}
