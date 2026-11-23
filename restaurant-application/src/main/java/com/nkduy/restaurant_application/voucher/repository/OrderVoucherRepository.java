package com.nkduy.restaurant_application.voucher.repository;

import com.nkduy.restaurant_application.voucher.entity.OrderVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderVoucherRepository extends JpaRepository<OrderVoucher, String> {
    boolean existsByCode(String code);
    Optional<OrderVoucher> findByCode(String code);
}
