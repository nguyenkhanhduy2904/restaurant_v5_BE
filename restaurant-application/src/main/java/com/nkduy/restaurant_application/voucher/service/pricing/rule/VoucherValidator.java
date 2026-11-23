package com.nkduy.restaurant_application.voucher.service.pricing.rule;

import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.service.pricing.model.Cart;
import com.nkduy.restaurant_application.voucher.service.pricing.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class VoucherValidator {
    public String validate(OrderVoucher voucher, Cart cart, UserContext userContext, ZonedDateTime now){
        Instant time = now.toInstant();
        if(!voucher.getStatus().equalsIgnoreCase("ACTIVE")){
            return "VOUCHER_NOT_ACTIVE";
        }
        if(time.isBefore(voucher.getStartAt())){
            return "NOT_STARTED";
        }
        if(time.isAfter(voucher.getEndAt())){
            return "EXPIRED";
        }
        if(voucher.getRules()==null){
            return null;
        }

        //TODO: other rules

        return null;


    }
}
