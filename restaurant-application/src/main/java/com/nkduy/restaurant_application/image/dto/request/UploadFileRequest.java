package com.nkduy.restaurant_application.image.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UploadFileRequest(
        @NotBlank
        String clientId,
        @NotBlank
        String fileName,

        @NotBlank
        String contentType,

        @NotNull
        @Positive

        Long fileSize
) {
}
