package com.nkduy.restaurant_application.voucher.service;

import com.nkduy.restaurant_application.mapper.VoucherMapper;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import com.nkduy.restaurant_application.voucher.dto.request.CreateUserOrderVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.response.CreateUserOrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserOrderVoucher;
import com.nkduy.restaurant_application.voucher.repository.OrderVoucherRepository;
import com.nkduy.restaurant_application.voucher.repository.UserOrderVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserOrderVoucherService {
    private final OrderVoucherRepository orderVoucherRepository;
    private final VoucherMapper voucherMapper;
    private final UserOrderVoucherRepository userOrderVoucherRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateUserOrderVoucherResponse claimVoucher(CreateUserOrderVoucherRequest request, String userId) {
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already have this voucher");
        }

        voucher.setCurrentClaim(voucher.getCurrentClaim() != null ? voucher.getCurrentClaim() + 1 : 1L);
        orderVoucherRepository.save(voucher);

        UserOrderVoucher userVoucher = UserOrderVoucher.builder()
                .voucher(voucher)
                .user(user)
                .build();

        return new CreateUserOrderVoucherResponse(userOrderVoucherRepository.save(userVoucher));
    }


    public UserOrderVoucher getUsableVoucher(String orderVoucherId, String userId){
        UserOrderVoucher userVoucher = userOrderVoucherRepository.findByUserIdAndVoucherId(userId, orderVoucherId);
        if(userVoucher == null){
            return null;
        }
        return userVoucher;
    }

    public void markUse(UserOrderVoucher userOrderVoucher) {
        //TODO: implement
    }
}
