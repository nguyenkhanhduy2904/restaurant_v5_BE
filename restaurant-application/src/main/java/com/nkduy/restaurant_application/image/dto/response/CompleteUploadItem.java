package com.nkduy.restaurant_application.image.dto.response;

import com.nkduy.restaurant_application.image.entity.UploadStatus;

public record CompleteUploadItem(

        String uploadId,
        UploadStatus status,
        String message
) {
}
