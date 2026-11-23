package com.nkduy.restaurant_application.auth.dto.response;

import com.nkduy.restaurant_application.user.dto.response.UserInfoResponse;

public record AuthResponse (
        String accessToken,
        String refreshToken,
        UserInfoResponse userInfoResponse
){
}
