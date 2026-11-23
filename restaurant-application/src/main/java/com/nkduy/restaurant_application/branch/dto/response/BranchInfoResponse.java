package com.nkduy.restaurant_application.branch.dto.response;

import java.math.BigDecimal;

public record BranchInfoResponse(
        String id,
        String name,
        String address,
        BigDecimal longitude,
        BigDecimal latitude,
        String status
) {
}
