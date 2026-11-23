package com.nkduy.restaurant_application.voucher.repository;

import com.nkduy.restaurant_application.voucher.entity.DeliveryVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryVoucherRepository extends JpaRepository<DeliveryVoucher, String> {
    boolean existsByCode(String code);
    Optional<DeliveryVoucher> findByCode(String code);
}
