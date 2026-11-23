package com.nkduy.restaurant_application.user.dto.response;

public record UserInfoResponse(
        String id,
        String displayName,
        String role,
        String status,
        String phone,
        String gender
) {
}
