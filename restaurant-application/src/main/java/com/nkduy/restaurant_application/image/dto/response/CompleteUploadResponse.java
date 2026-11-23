package com.nkduy.restaurant_application.image.dto.response;

import java.util.List;

public record CompleteUploadResponse(
        List<CompleteUploadItem> uploads
) {
}
