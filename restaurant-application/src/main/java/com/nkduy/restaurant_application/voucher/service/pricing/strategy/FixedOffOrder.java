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
public class FixedOffOrder extends AbstractStrategy<FixedOffOrder.Params>{

    public record Params(@NotNull @DecimalMin("1") BigDecimal amount) {}

    public FixedOffOrder(ObjectMapper mapper, Validator validator) {
        super(mapper, validator, Params.class);
    }

    @Override
    protected DiscountResult compute(Cart cart, Params p) {
        // lineId -> line price. LinkedHashMap keeps cart order, so ties are stable.
        Map<String, BigDecimal> pricePerLine = new LinkedHashMap<>();
        for (CartLine line : cart.lines()) {
            pricePerLine.put(line.id(), line.total());
        }

        // A 50k voucher on a 30k cart can only discount 30k
        BigDecimal amount = p.amount().min(cart.cartTotal());

        return DiscountResult.line(DiscountAllocator.split(amount, pricePerLine));
    }

    @Override
    public VoucherType type() { return VoucherType.FIXED_OFF_ORDER; }
}
