package com.nkduy.restaurant_application.image.dto.response;

import java.time.Instant;

public record PresignedUploadItem(
        String clientId,

        String uploadId,

        String uploadUrl,

        String objectKey,

        String method,

        Instant expiresAt
) {
}
