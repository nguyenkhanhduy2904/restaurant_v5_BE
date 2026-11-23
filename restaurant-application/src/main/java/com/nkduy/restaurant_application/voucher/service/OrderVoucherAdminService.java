package com.nkduy.restaurant_application.voucher.service;

import com.nkduy.restaurant_application.mapper.VoucherMapper;
import com.nkduy.restaurant_application.voucher.dto.request.CreateOrderVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.response.CreateOrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import com.nkduy.restaurant_application.voucher.repository.OrderVoucherRepository;
import com.nkduy.restaurant_application.voucher.service.pricing.model.VoucherType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderVoucherAdminService {
    private final OrderVoucherRepository orderVoucherRepository;
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
                .status(request.status())
                .rules(request.rules())
                .param(request.params())
                .build();

        OrderVoucher saved = orderVoucherRepository.save(voucher);
        return new CreateOrderVoucherResponse(voucherMapper.toOrderVoucherResponse(saved));
    }

    private void validateOrderVoucherRequest(CreateOrderVoucherRequest request) {
        //TODO: validate request

    }


}
