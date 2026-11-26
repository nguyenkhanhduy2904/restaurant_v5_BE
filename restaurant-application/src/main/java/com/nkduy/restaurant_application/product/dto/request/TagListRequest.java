package com.nkduy.restaurant_application.product.dto.request;

import java.util.List;

public record TagListRequest(
        List<String> tagIds
) {
}
