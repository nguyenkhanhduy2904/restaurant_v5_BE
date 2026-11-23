package com.nkduy.restaurant_application.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StorageUrlService {

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    public String getPublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        return publicUrl.replaceAll("/+$", "")
                + "/"
                + objectKey;
    }
}
