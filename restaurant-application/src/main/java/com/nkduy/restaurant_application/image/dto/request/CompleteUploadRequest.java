package com.nkduy.restaurant_application.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CompleteUploadRequest(
        @NotEmpty
        List<@NotBlank String> uploadIds
) {
}
