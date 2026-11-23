package com.nkduy.restaurant_application;

import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.service.pricing.PricingService;
import com.nkduy.restaurant_application.voucher.service.pricing.model.*;
import com.nkduy.restaurant_application.voucher.service.pricing.rule.VoucherValidator;
import com.nkduy.restaurant_application.voucher.service.pricing.strategy.FixedOffOrder;
import com.nkduy.restaurant_application.voucher.service.pricing.strategy.PercentOffOrder;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class PricingServiceTest {

    static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    static final ObjectMapper MAPPER = JsonMapper.builder().build();      // tools.jackson.databind.json.JsonMapper

    PricingService pricing = new PricingService(
            List.of(new PercentOffOrder(MAPPER, VALIDATOR), new FixedOffOrder(MAPPER, VALIDATOR)),
            new VoucherValidator(),
            Clock.fixed(Instant.parse("2026-11-04T04:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));

    @Test
    void thirtyPercentOff_cappedAtTenThousand() {
        Cart cart = new Cart(List.of(
                new CartLine("0", "pho", "Pho", bd(30_000), 1, List.of()),
                new CartLine("1", "bun", "Bun", bd(60_000), 1, List.of()),
                new CartLine("2", "tea", "Tea", bd(15_000), 1, List.of())),
                "DELIVERY", "branch-1", bd(15_000));

        OrderVoucher voucher = OrderVoucher.builder()
                .discountType(VoucherType.PERCENTAGE_OFF_ORDER)   // adjust to your enum
                .param(Map.of("percentage", 30))
                .maxDiscount(bd(10_000))
                .status("ACTIVE")                                 // adjust to your entity
                .startAt(Instant.parse("2026-11-01T00:00:00Z"))
                .endAt(Instant.parse("2026-11-30T00:00:00Z"))
                .build();

        PriceBreakDown price = pricing.calculate(cart, voucher, new UserContext("u1", false, bd(0), null));

        assertNull(price.rejectReason());
        assertEquals(0, bd(2_857).compareTo(price.discountPerLine().get("0")));
        assertEquals(0, bd(5_715).compareTo(price.discountPerLine().get("1")));
        assertEquals(0, bd(1_428).compareTo(price.discountPerLine().get("2")));
        assertEquals(0, bd(110_000).compareTo(price.finalTotal()));   // 105,000 + 15,000 - 10,000
    }

    @Test
    void noVoucher_givesFullPrice() {
        // same cart, voucher == null -> finalTotal 120,000, discountPerLine empty
    }

    static BigDecimal bd(long v) { return BigDecimal.valueOf(v); }
}
