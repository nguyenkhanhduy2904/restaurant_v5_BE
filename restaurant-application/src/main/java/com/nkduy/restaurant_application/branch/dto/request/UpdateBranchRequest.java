package com.nkduy.restaurant_application.branch.dto.request;

import com.nkduy.restaurant_application.branch.entity.BranchStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateBranchRequest(

        @Size(max = 255)
        String name,


        @Size(max = 255)
        String address,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
        BigDecimal longitude,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
        BigDecimal latitude,

        Boolean haveEveryProduct,//using Boolean instead of boolean allow null value, when FE wont send this field


        Set<String> excludedProductIds,

        BranchStatus status


) {
}
