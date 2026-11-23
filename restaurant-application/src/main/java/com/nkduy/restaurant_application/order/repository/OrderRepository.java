package com.nkduy.restaurant_application.order.repository;

import com.nkduy.restaurant_application.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String>{
    Page<Order> findByUserId(String userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(String userId, String status, Pageable pageable);

    Page<Order> findByStatus(String status, Pageable pageable);
}
