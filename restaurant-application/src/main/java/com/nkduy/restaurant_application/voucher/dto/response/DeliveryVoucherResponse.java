package com.nkduy.restaurant_application.voucher.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record DeliveryVoucherResponse(
        String id,
        String name,
        String discountType,
        BigDecimal discountValue,
        Instant startAt,
        Instant endAt,
        Long maxClaim,
        Long currentClaim,
        BigDecimal minDeliveryFee,
        BigDecimal maxDiscount,
        String status,
        String code
) {
}
