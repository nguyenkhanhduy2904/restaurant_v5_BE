package com.nkduy.restaurant_application.image.dto.response;

import java.util.List;

public record PresignedUploadResponse(
        List<PresignedUploadItem> uploads
) {
}
