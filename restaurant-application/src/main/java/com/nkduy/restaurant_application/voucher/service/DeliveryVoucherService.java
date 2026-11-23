package com.nkduy.restaurant_application.voucher.service;

import com.nkduy.restaurant_application.mapper.VoucherMapper;
import com.nkduy.restaurant_application.user.entity.User;
import com.nkduy.restaurant_application.user.repository.UserRepository;
import com.nkduy.restaurant_application.voucher.dto.request.CreateDeliveryVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.request.CreateUserDeliveryVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.response.CreateDeliveryVoucherResponse;
import com.nkduy.restaurant_application.voucher.dto.response.CreateUserDeliveryVoucherResponse;
import com.nkduy.restaurant_application.voucher.entity.DeliveryVoucher;
import com.nkduy.restaurant_application.voucher.entity.UserDeliveryVoucher;
import com.nkduy.restaurant_application.voucher.repository.DeliveryVoucherRepository;
import com.nkduy.restaurant_application.voucher.repository.UserDeliveryVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DeliveryVoucherService {
    private final DeliveryVoucherRepository deliveryVoucherRepository;
    private final UserDeliveryVoucherRepository userDeliveryVoucherRepository;
    private final UserRepository userRepository;
    private final VoucherMapper voucherMapper;

    @Transactional
    public CreateDeliveryVoucherResponse createDeliveryVoucher(CreateDeliveryVoucherRequest request) {
        validateDeliveryVoucherRequest(request);

        DeliveryVoucher voucher = DeliveryVoucher.builder()
                .name(request.name())
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .code(request.code())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .maxClaim(request.maxClaim())
                .minDeliveryFee(request.minDeliveryFee())
                .maxDiscount(request.maxDiscount())
                .status(request.status() != null ? request.status() : "ACTIVE")
                .build();

        DeliveryVoucher saved = deliveryVoucherRepository.save(voucher);
        return new CreateDeliveryVoucherResponse(voucherMapper.toDeliveryVoucherResponse(saved));
    }

    private void validateDeliveryVoucherRequest(CreateDeliveryVoucherRequest request) {
        if (request.endAt().isBefore(request.startAt()) || request.endAt().equals(request.startAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        if (deliveryVoucherRepository.existsByCode(request.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher code already exists");
        }

        if ("PERCENTAGE".equalsIgnoreCase(request.discountType())) {
            if (request.discountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Percentage discount cannot exceed 100%");
            }
        }
    }

    @Transactional
    public CreateUserDeliveryVoucherResponse createUserDeliveryVoucher(CreateUserDeliveryVoucherRequest request, String userId) {
//        if (userId == null || userId.isBlank()) {
//            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
//        }already check by spring security

        DeliveryVoucher voucher = deliveryVoucherRepository.findByCode(request.code())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Voucher not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is not active");
        }

        Instant now = Instant.now();
//        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is not yet active");
//        }still allow to claim voucher before start date( start date is more like when voucher is available)

        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher has expired");
        }

        if (voucher.getMaxClaim() != null && voucher.getCurrentClaim() != null && voucher.getCurrentClaim() >= voucher.getMaxClaim()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Voucher claim limit reached");
        }

        if (userDeliveryVoucherRepository.existsByUserIdAndVoucherId(user.getId(), voucher.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already claimed this voucher");
        }

        voucher.setCurrentClaim(voucher.getCurrentClaim() != null ? voucher.getCurrentClaim() + 1 : 1L);
        deliveryVoucherRepository.save(voucher);

        UserDeliveryVoucher userVoucher = UserDeliveryVoucher.builder()
                .voucher(voucher)
                .user(user)
                .build();

        return new CreateUserDeliveryVoucherResponse(userDeliveryVoucherRepository.save(userVoucher));
    }

    public boolean isVoucherUsable(String userVoucherId, String userId, BigDecimal deliveryFee){
        UserDeliveryVoucher userVoucher = userDeliveryVoucherRepository.findById(userVoucherId).orElse(null);
        if(userVoucher == null || !userVoucher.getUser().getId().equals(userId)){
            return false;
        }
        if (!"USABLE".equalsIgnoreCase(userVoucher.getStatus())) {
            return false;
        }
        DeliveryVoucher voucher = userVoucher.getVoucher();
        if (voucher == null || !"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
            return false;
        }
        Instant now = Instant.now();
        if (voucher.getStartAt() != null && now.isBefore(voucher.getStartAt())) {
            return false;
        }
        if (voucher.getEndAt() != null && now.isAfter(voucher.getEndAt())) {
            return false;
        }

        if (voucher.getMinDeliveryFee() != null && voucher.getMinDeliveryFee().compareTo(deliveryFee) > 0) {
            return false;
        }
        return true;


    }
    public DeliveryVoucher getDeliveryVoucherById(String id){
        return deliveryVoucherRepository.findById(id).orElse(null);
    }

    @Transactional
    public UserDeliveryVoucher useDeliveryVoucher(String userDeliveryVoucherId, String userId) {
        UserDeliveryVoucher userVoucher = userDeliveryVoucherRepository.findById(userDeliveryVoucherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User delivery voucher not found"));

        if (!userVoucher.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this voucher");
        }

        if (!"USABLE".equalsIgnoreCase(userVoucher.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voucher is not in USABLE status");
        }

        userVoucher.setStatus("USED");
        return userDeliveryVoucherRepository.save(userVoucher);
    }

    @Transactional
    public void cancelDeliveryVoucher(String userDeliveryVoucherId) {
        UserDeliveryVoucher userVoucher = userDeliveryVoucherRepository.findById(userDeliveryVoucherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User delivery voucher not found"));

        userVoucher.setStatus("UNUSED");
        userDeliveryVoucherRepository.save(userVoucher);

    }
}
