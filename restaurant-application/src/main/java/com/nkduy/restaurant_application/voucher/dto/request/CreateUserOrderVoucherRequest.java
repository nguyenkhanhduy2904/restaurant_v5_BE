package com.nkduy.restaurant_application.voucher.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserOrderVoucherRequest(
        @NotBlank(message = "Voucher code is required")
        @Size(max = 255, message = "Code cannot exceed 255 characters")
        String code
){
}
