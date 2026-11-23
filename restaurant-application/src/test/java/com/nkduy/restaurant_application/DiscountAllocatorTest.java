package com.nkduy.restaurant_application;

//package com.nkduy.restaurant_application.voucher.service.pricing.helper;

import com.nkduy.restaurant_application.voucher.service.pricing.helper.DiscountAllocator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DiscountAllocatorTest {

    @Test
    void splitsProportionallyAndPartsAddUpExactly() {
        Map<String, BigDecimal> prices = new LinkedHashMap<>();
        prices.put("A", bd(30_000));
        prices.put("B", bd(60_000));
        prices.put("C", bd(15_000));

        Map<String, BigDecimal> result = DiscountAllocator.split(bd(10_000), prices);

        assertEquals(0, bd(2_857).compareTo(result.get("A")));
        assertEquals(0, bd(5_715).compareTo(result.get("B")));   // got the leftover dong
        assertEquals(0, bd(1_428).compareTo(result.get("C")));
        assertEquals(0, bd(10_000).compareTo(sum(result)));      // the important one
    }

    @Test
    void singleLineGetsEverything() {
        Map<String, BigDecimal> result = DiscountAllocator.split(bd(10_000), Map.of("A", bd(80_000)));
        assertEquals(0, bd(10_000).compareTo(result.get("A")));
    }

    @Test
    void zeroAmountGivesEmptyResult() {
        assertTrue(DiscountAllocator.split(BigDecimal.ZERO, Map.of("A", bd(1))).isEmpty());
    }

    private static BigDecimal bd(long v) { return BigDecimal.valueOf(v); }
    private static BigDecimal sum(Map<String, BigDecimal> m) {
        return m.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}