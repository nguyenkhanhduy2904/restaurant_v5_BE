package com.nkduy.restaurant_application.order.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.order.dto.model.OrderDto;
import com.nkduy.restaurant_application.order.dto.request.CreateOrderRequest;
import com.nkduy.restaurant_application.order.dto.request.PreviewOrderRequest;
import com.nkduy.restaurant_application.order.dto.request.UpdateOrderStatusRequest;
import com.nkduy.restaurant_application.order.dto.response.CreateOrderResponse;
import com.nkduy.restaurant_application.order.dto.response.PreviewOrderResponse;
import com.nkduy.restaurant_application.order.dto.response.UpdateOrderStatusResponse;
import com.nkduy.restaurant_application.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/preview-order")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PreviewOrderResponse> previewOrder(@Valid @RequestBody PreviewOrderRequest request, @AuthenticationPrincipal String userId){
        return ApiResponse.ok(orderService.previewOrder(request, userId));
    }

    @PostMapping("/create-order")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, @AuthenticationPrincipal String userId){
        return ApiResponse.ok(orderService.createOrder(request, userId));
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<OrderDto> getOrder(@PathVariable String id){
        return ApiResponse.ok(orderService.getOrder(id));
    }

    @GetMapping("/history")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<OrderDto>> getOrderHistory(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) String status,
            @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC)
            Pageable pageable){

        return ApiResponse.ok(orderService.getOrderHistory(userId,status, pageable));
    }

    @PostMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<String> cancelOrder(@PathVariable("id") String id, @AuthenticationPrincipal String userId){
        return ApiResponse.ok(orderService.cancelOrder(id, userId));
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Page<OrderDto>> getOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(
            size = 20,
            sort = "createdAt",
            direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ApiResponse.ok(orderService.getOrders(status, pageable));
    }

    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UpdateOrderStatusResponse> updateOrderStatus(@Valid @RequestBody UpdateOrderStatusRequest status, @PathVariable String id){
        return ApiResponse.ok(orderService.updateOrderStatus(status, id));
    }
}
