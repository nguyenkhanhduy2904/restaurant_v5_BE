package com.nkduy.restaurant_application.voucher.model;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record VoucherRules(
        BigDecimal minOrderPrice,
        Map<DayOfWeek, List<TimeRange>> schedules,
        Set<String> branchIds,
        Boolean isNewCustomerOnly
) {
}

