package com.nkduy.restaurant_application.voucher.model;

import java.time.LocalTime;

public record TimeRange(
        LocalTime from,
        LocalTime to
) {
}
