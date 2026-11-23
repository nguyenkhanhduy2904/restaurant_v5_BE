package com.nkduy.restaurant_application.mapper;

import com.nkduy.restaurant_application.user.dto.response.UserInfoResponse;
import com.nkduy.restaurant_application.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(
                user.getId()
                , user.getDisplayName()
                , user.getRole()
                , user.getStatus()
                , user.getPhone()
                , user.getGender());
    }
}
