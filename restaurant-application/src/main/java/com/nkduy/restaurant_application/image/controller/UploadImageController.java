package com.nkduy.restaurant_application.image.controller;

import com.nkduy.restaurant_application.auth.dto.ApiResponse;
import com.nkduy.restaurant_application.image.dto.request.CompleteUploadRequest;
import com.nkduy.restaurant_application.image.dto.request.PresignedUploadRequest;
import com.nkduy.restaurant_application.image.dto.response.CompleteUploadResponse;
import com.nkduy.restaurant_application.image.dto.response.PresignedUploadResponse;
import com.nkduy.restaurant_application.image.service.UploadImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/uploads")
@RequiredArgsConstructor
public class UploadImageController {

    private final UploadImageService uploadImageService;

    @PostMapping("/product")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<PresignedUploadResponse> requestProductUpload(@AuthenticationPrincipal String id, @Valid @RequestBody PresignedUploadRequest request){
        return ApiResponse.ok(uploadImageService.requestProductUpload( id, request));
    }

    @PostMapping("/complete")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CompleteUploadResponse> confirmUpload(@AuthenticationPrincipal String id,@Valid @RequestBody CompleteUploadRequest request){
        return ApiResponse.ok(uploadImageService.confirmUpload(id,request));

    }



}
