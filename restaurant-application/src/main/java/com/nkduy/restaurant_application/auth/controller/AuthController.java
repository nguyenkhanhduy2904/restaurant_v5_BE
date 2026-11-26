package com.nkduy.restaurant_application.auth.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.auth.dto.request.LoginRequest;
import com.nkduy.restaurant_application.auth.dto.request.RefreshTokenRequest;
import com.nkduy.restaurant_application.auth.dto.request.RegisterRequest;
import com.nkduy.restaurant_application.auth.dto.response.AuthResponse;
import com.nkduy.restaurant_application.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }
    @PostMapping("/register-admin")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.registerAdmin(request));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request){
        return ApiResponse.ok(authService.login(request));
    }
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request){
        return ApiResponse.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshTokenRequest request){
        return ApiResponse.ok(authService.logOut(request));
    }

}
