package com.nkduy.restaurant_application.order.dto.model;

import com.nkduy.restaurant_application.branch.entity.Branch;
import com.nkduy.restaurant_application.order.dto.response.PreviewOrderResponse;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.voucher.entity.UserOrderVoucher;
import com.nkduy.restaurant_application.voucher.service.pricing.model.PriceBreakDown;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record OrderValidationResult(
        boolean isValid,                 // false only for hard errors (bad user, product, address...)
        List<String> errors,
        User user,
        Branch branch,
        List<ValidatedOrderItem> items,
        int totalItemCount,
        BigDecimal deliveryDistance,
        UserOrderVoucher userVoucher,    // null if no voucher was sent or it can't be used
        PriceBreakDown price,            // null only when the result is invalid
        String voucherError              // null when OK, e.g. "MIN_SUBTOTAL_NOT_MET"
) {
    public OrderValidationResult {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static OrderValidationResult invalid(List<String> errors) {
        return new OrderValidationResult(
                false, errors,
                null, null,              // user, branch
                List.of(), 0,            // items, totalItemCount
                null,                    // deliveryDistance
                null,                    // userVoucher
                null,                    // price
                null);                   // voucherError
    }


    public PreviewOrderResponse toPreviewResponse() {
        return new PreviewOrderResponse(
                isValid,
                errors,
                items.stream().map(ValidatedOrderItem::toOrderItemDto).toList(),
                price

        );
    }
}
