package com.nkduy.restaurant_application.order.repository;

import com.nkduy.restaurant_application.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OrderitemRepository extends JpaRepository<OrderItem, String> {
    List<OrderItem> findByOrderId(String orderId);
    List<OrderItem> findByOrder_IdIn(Collection<String> orderIds);
}
