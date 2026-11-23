package com.nkduy.restaurant_application.mapper;

import com.nkduy.restaurant_application.voucher.dto.response.DeliveryVoucherResponse;
import com.nkduy.restaurant_application.voucher.dto.response.OrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.entity.DeliveryVoucher;
import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {
    public OrderVoucherResponse toOrderVoucherResponse(OrderVoucher voucher){
        return new OrderVoucherResponse(
                voucher.getId(),
                voucher.getName(),
                voucher.getDiscountType().toString(),
                voucher.getStartAt(),
                voucher.getEndAt(),
                voucher.getMaxClaim(),
                voucher.getCurrentClaim(),
                voucher.getMaxDiscount(),
                voucher.getStatus(),
                voucher.getCode(),
                voucher.getRules(),
                voucher.getParam());

    }

    public DeliveryVoucherResponse toDeliveryVoucherResponse(DeliveryVoucher voucher){
        return new DeliveryVoucherResponse(
                voucher.getId(),
                voucher.getName(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getStartAt(),
                voucher.getEndAt(),
                voucher.getMaxClaim(),
                voucher.getCurrentClaim(),
                voucher.getMinDeliveryFee(),
                voucher.getMaxDiscount(),
                voucher.getStatus(),
                voucher.getCode()
        );

    }
}
