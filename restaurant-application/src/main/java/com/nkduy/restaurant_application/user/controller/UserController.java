package com.nkduy.restaurant_application.user.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.user.dto.request.AvatarChangeRequest;
import com.nkduy.restaurant_application.user.dto.request.UpdateProfileRequest;
import com.nkduy.restaurant_application.user.dto.response.AvatarChangeResponse;
import com.nkduy.restaurant_application.user.dto.response.UserInfoResponse;
import com.nkduy.restaurant_application.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserInfoResponse> getMe(@AuthenticationPrincipal String id){
        return ApiResponse.ok(userService.getMe(id));
    }


    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserInfoResponse> editProfile(@AuthenticationPrincipal String id,@Valid @RequestBody UpdateProfileRequest request){
        return ApiResponse.ok(userService.editProfile(id, request));
    }


    @PatchMapping("/change-avatar")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AvatarChangeResponse> changeAvatar(@AuthenticationPrincipal String id, @Valid @RequestBody AvatarChangeRequest request){
        return ApiResponse.ok(userService.changeAvatar(id, request));
    }





}
