package com.nkduy.restaurant_application.mapper;

import com.nkduy.restaurant_application.order.dto.model.OrderDto;
import com.nkduy.restaurant_application.order.dto.model.OrderItemDto;
import com.nkduy.restaurant_application.order.dto.model.ValidatedOrderItem;
import com.nkduy.restaurant_application.order.entity.Order;
import com.nkduy.restaurant_application.order.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class OrderMapper {
    public OrderDto toOrderDto(Order order, List<OrderItemDto> items){
        if (order == null) {
            return null;
        }
        return new OrderDto(
                order.getId(),
                order.getUser() != null ? order.getUser().getId() : null,
                order.getBranch() != null ? order.getBranch().getId() : null,
                items != null ? items : Collections.emptyList(),
                order.getTotalItem() != null ? order.getTotalItem() : 0,
                order.getPriceBeforeDiscount(),
                order.getDeliveryDistance(),
                order.getDeliveryFee(),
                order.getOrderDiscounted(),
                order.getDeliveryDiscount(),
                order.getFinalPrice(),
                order.getAddress(),
                order.getContactPhone(),
                order.getContactName(),
                order.getStatus(),
                order.getUserOrderVoucher() != null ? order.getUserOrderVoucher().getId() : null,
                order.getUserDeliveryVoucher() != null ? order.getUserDeliveryVoucher().getId() : null
        );
    }
    public OrderItemDto toOrderItemDto(ValidatedOrderItem item){
        return new OrderItemDto(
                item.product().getId(),
                item.product().getName(),
                item.cartLine().quantity(),
                item.cartLine().unitPrice(),
                item.note()
        );

    }
    public OrderItemDto toOrderItemDto(OrderItem orderItem){
        return new OrderItemDto(
               orderItem.getProduct().getId(),
                orderItem.getName(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getNote()
        );

    }

}
