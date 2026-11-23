package com.nkduy.restaurant_application.voucher.repository;

import com.nkduy.restaurant_application.voucher.entity.UserDeliveryVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeliveryVoucherRepository extends JpaRepository<UserDeliveryVoucher, String> {
    boolean existsByUserIdAndVoucherId(String userId, String voucherId);
}
