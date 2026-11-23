package com.nkduy.restaurant_application.voucher.dto.response;

import com.nkduy.restaurant_application.voucher.model.VoucherRules;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record OrderVoucherResponse(
        String id,
        String name,
        String discountType,
//        BigDecimal discountValue,
        Instant startAt,
        Instant endAt,
        Long maxClaim,
        Long currentClaim,
//        BigDecimal minOrderPrice,
        BigDecimal maxDiscount,
        String status,
        String code,
        VoucherRules rules,
        Map<String, Object> params
) {
}
