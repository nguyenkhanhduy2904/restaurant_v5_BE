package com.nkduy.restaurant_application.user.dto.request;

public record UpdateProfileRequest(
        String displayName,
        String phone,
        String gender
) {
}
