package com.nkduy.restaurant_application.voucher.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.voucher.dto.request.CreateDeliveryVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.request.CreateOrderVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.request.CreateUserDeliveryVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.request.CreateUserOrderVoucherRequest;
import com.nkduy.restaurant_application.voucher.dto.response.CreateDeliveryVoucherResponse;
import com.nkduy.restaurant_application.voucher.dto.response.CreateOrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.dto.response.CreateUserDeliveryVoucherResponse;
import com.nkduy.restaurant_application.voucher.dto.response.CreateUserOrderVoucherResponse;
import com.nkduy.restaurant_application.voucher.service.DeliveryVoucherService;
import com.nkduy.restaurant_application.voucher.service.OrderVoucherAdminService;
import com.nkduy.restaurant_application.voucher.service.OrderVoucherService;
import com.nkduy.restaurant_application.voucher.service.UserOrderVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {
//    private final OrderVoucherService orderVoucherService;
//    private final DeliveryVoucherService deliveryVoucherService;

    private final OrderVoucherAdminService orderVoucherAdminServiceService;
    private final UserOrderVoucherService userOrderVoucherService;

    @PostMapping("/order/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateOrderVoucherResponse> createOrderVoucher(@Valid @RequestBody CreateOrderVoucherRequest request){
        return ApiResponse.ok(orderVoucherAdminServiceService.createOrderVoucher(request));
    }

    @PostMapping("/order/claim")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateUserOrderVoucherResponse> createUserOrderVoucher(@Valid @RequestBody CreateUserOrderVoucherRequest request, @AuthenticationPrincipal String userId ){
        return ApiResponse.ok(userOrderVoucherService.claimVoucher(request, userId));
    }

//    @PostMapping("/delivery/create")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ApiResponse<CreateDeliveryVoucherResponse> createDeliveryVoucher(@Valid @RequestBody CreateDeliveryVoucherRequest request){
//        return ApiResponse.ok(deliveryVoucherService.createDeliveryVoucher(request));
//    }
//
//    @PostMapping("/delivery/claim")
//    @ResponseStatus(HttpStatus.CREATED)
//    public ApiResponse<CreateUserDeliveryVoucherResponse> createUserDeliveryVoucher(@Valid @RequestBody CreateUserDeliveryVoucherRequest request, @AuthenticationPrincipal String userId ){
//        return ApiResponse.ok(deliveryVoucherService.createUserDeliveryVoucher(request, userId ));
//    }

}
