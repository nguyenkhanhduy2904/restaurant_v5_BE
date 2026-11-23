package com.nkduy.restaurant_application.order.dto.response;

import com.nkduy.restaurant_application.order.dto.model.OrderItemDto;
import com.nkduy.restaurant_application.voucher.service.pricing.model.Cart;
import com.nkduy.restaurant_application.voucher.service.pricing.model.CartLine;
import com.nkduy.restaurant_application.voucher.service.pricing.model.PriceBreakDown;

import java.math.BigDecimal;
import java.util.List;

public record PreviewOrderResponse(
        boolean isValid,
        List<String> errors,
//
//        BigDecimal orderPriceBeforeDiscount,// PriceBreakDown.total() already cover this
//

        List<OrderItemDto> items,
        PriceBreakDown priceBreakDown
) {
}
