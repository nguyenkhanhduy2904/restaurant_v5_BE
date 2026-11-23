package com.nkduy.restaurant_application.voucher.repository;

import com.nkduy.restaurant_application.voucher.entity.UserOrderVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOrderVoucherRepository extends JpaRepository<UserOrderVoucher, String> {
    boolean existsByUserIdAndVoucherId(String userId, String voucherId);
    UserOrderVoucher findByUserIdAndVoucherId(String userId, String voucherId);
}
