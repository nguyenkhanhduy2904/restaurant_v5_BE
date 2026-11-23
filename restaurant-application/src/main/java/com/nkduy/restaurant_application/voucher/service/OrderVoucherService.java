package com.nkduy.restaurant_application.voucher.service;

import com.nkduy.restaurant_application.mapper.VoucherMapper;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import com.nkduy.restaurant_application.voucher.dto.request.CreateOrderVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.request.CreateUserOrderVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.response.CreateOrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.dto.response.CreateUserOrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.entity.DeliveryVoucher;
import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserDeliveryVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserOrderVoucher;
import com.nkduy.restaurant_application.voucher.repository.OrderVoucherRepository;
import com.nkduy.restaurant_application.voucher.repository.UserOrderVoucherRepository;
import com.nkduy.restaurant_application.voucher.service.pricing.model.VoucherType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderVoucherService {
    private final OrderVoucherRepository orderVoucherRepository;
    private final UserOrderVoucherRepository userOrderVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Transactional
    public CreateOrderVoucherResponse createOrderVoucher(CreateOrderVoucherRequest request) {
        validateOrderVoucherRequest(request);

        OrderVoucher voucher = OrderVoucher.builder()
                .name(request.name())
                .discountType(VoucherType.valueOf(request.discountType()))
                .code(request.code())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .maxClaim(request.maxClaim())
                .maxDiscount(request.maxDiscount())
                .status(request.status() != null ? request.status() : "ACTIVE")
                .param(request.params())
                .rules(request.rules())
                .build();

        OrderVoucher saved = orderVoucherRepository.save(voucher);
        return new CreateOrderVoucherResponse(voucherMapper.toOrderVoucherResponse(saved));
    }

    private void validateOrderVoucherRequest(CreateOrderVoucherRequest request) {
        if (request.endAt().isBefore(request.startAt()) || request.endAt().equals(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

//        if (orderVoucherRepository.existsByCode(request.code())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher code already exists");
//        }
//
//        if ("PERCENTAGE".equalsIgnoreCase(request.discountType())) {
//            if (request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentage discount cannot exceed 100%");
//            }
//        }
    }

    @Transactional
    public CreateUserOrderVoucherResponse createUserOrderVoucher(CreateUserOrderVoucherRequest request, String userId) {
        OrderVoucher voucher = orderVoucherRepository.findByCode(request.code())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is not active");
        }

        Instant now = Instant.now();

        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher has expired");
        }

        if (voucher.getMaxClaim() != null && voucher.getCurrentClaim() != null && voucher.getCurrentClaim() >= voucher.getMaxClaim()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher claim limit reached");
        }

        if (userOrderVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucher.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already USABLE this voucher");
        }

        voucher.setCurrentClaim(voucher.getCurrentClaim() != null ? voucher.getCurrentClaim() + 1 : 1L);
        orderVoucherRepository.save(voucher);

        UserOrderVoucher userVoucher = UserOrderVoucher.builder()
                .voucher(voucher)
                .user(user)
                .build();

        return new CreateUserOrderVoucherResponse(userOrderVoucherRepository.save(userVoucher));
    }

//    public boolean isVoucherUsable(String orderVoucherId, String userId, BigDecimal totalPrice) {
//        UserOrderVoucher userVoucher = userOrderVoucherRepository.findById(orderVoucherId).orElse(null);
//        if (userVoucher == null || !userVoucher.getUser().getId().equals(userId)) {
//            return false;
//        }
//        if (!"USABLE".equalsIgnoreCase(userVoucher.getStatus())) {
//            return false;
//        }
//        OrderVoucher voucher = userVoucher.getVoucher();
//        if (voucher == null || !"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
//            return false;
//        }
//        Instant now = Instant.now();
//        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
//            return false;
//        }
//        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
//            return false;
//        }
//
//        if (voucher.getMinOrderPrice() != null && voucher.getMinOrderPrice().compareTo(totalPrice) > 0) {
//            return false;
//        }
//        return true;
//    }

    // TODO: Implement calculateOrderDiscount(String orderVoucherId, BigDecimal totalPrice)
    // - Calculate discount amount based on discountType (PERCENTAGE vs FIXED), discountValue, and maxDiscount cap
    public OrderVoucher getOrderVoucherById(String id){
        return orderVoucherRepository.findById(id).orElse(null);
    }

    @Transactional
    public UserOrderVoucher useOrderVoucher(String userOrderVoucherId, String userId) {
        UserOrderVoucher userVoucher = userOrderVoucherRepository.findById(userOrderVoucherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User order voucher not found"));

        if (!userVoucher.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this voucher");
        }

        if (!"USABLE".equalsIgnoreCase(userVoucher.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is not in USABLE status");
        }

        userVoucher.setStatus("USED");
        return userOrderVoucherRepository.save(userVoucher);
    }

    @Transactional
    public void cancelOrderVoucher(String userOrderVoucherId) {
        UserOrderVoucher userVoucher = userOrderVoucherRepository.findById(userOrderVoucherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User delivery voucher not found"));

        userVoucher.setStatus("UNUSED");
        userOrderVoucherRepository.save(userVoucher);

    }


    // TODO: Implement useOrderVoucher(String orderVoucherId, String userId)
    // - Transition UserOrderVoucher status from "USABLE" to "USED" upon successful order creation/checkout

    // TODO: Implement releaseOrderVoucher(String orderVoucherId, String userId)
    // - Revert UserOrderVoucher status from "USED" back to "USABLE" if an order is cancelled or payment fails

    // TODO: Implement getUsableVouchersForUser(String userId, BigDecimal totalPrice)
    // - Retrieve all valid, active, and applicable vouchers for a user based on the current order finalTotal price
}
