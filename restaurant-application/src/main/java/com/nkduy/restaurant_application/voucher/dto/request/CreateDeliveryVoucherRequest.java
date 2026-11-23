package com.nkduy.restaurant_application.voucher.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateDeliveryVoucherRequest(
        @NotBlank(message = "Voucher name is required")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,

        @NotBlank(message = "Discount type is required")
        @Pattern(regexp = "^(PERCENTAGE|FIXED_AMOUNT)$", message = "Discount type must be either PERCENTAGE or FIXED_AMOUNT")
        String discountType,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
        BigDecimal discountValue,

        @NotBlank(message = "Voucher code is required")
        @Size(max = 255, message = "Code cannot exceed 255 characters")
        String code,

        @NotNull(message = "Start date is required")
        Instant startAt,

        @NotNull(message = "End date is required")
        Instant endAt,

        @NotNull(message = "Max claim is required")
        @Min(value = 1, message = "Max claim must be at least 1")
        Long maxClaim,

        @DecimalMin(value = "0.0", message = "Min delivery fee must be >= 0")
        BigDecimal minDeliveryFee,

        @DecimalMin(value = "0.0", message = "Max discount must be >= 0")
        BigDecimal maxDiscount,

        @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be either ACTIVE or INACTIVE")
        String status
) {
}
