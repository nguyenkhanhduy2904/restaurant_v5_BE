package com.nkduy.restaurant_application.image.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PresignedUploadRequest(
        @NotEmpty
        @Size(max = 10)
        List<@Valid UploadFileRequest> files
) {
}
