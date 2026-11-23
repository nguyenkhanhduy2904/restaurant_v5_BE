package com.nkduy.restaurant_application.order.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class DeliveryService {

    public BigDecimal calculateDistance(BigDecimal fromLat, BigDecimal fromLng, BigDecimal toLat, BigDecimal toLng){
        double lat1 = Math.toRadians(fromLat.doubleValue());
        double lon1 = Math.toRadians(fromLng.doubleValue());
        double lat2 = Math.toRadians(toLat.doubleValue());
        double lon2 = Math.toRadians(toLng.doubleValue());

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1)
                * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLon / 2), 2);

        // Prevent floating-point rounding from making a slightly
        // negative value or a value greater than 1.
        a = Math.max(0.0, Math.min(1.0, a));

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1.0 - a)
        );

        double earthRadiusKm = 6371.0;
        double distanceKm = earthRadiusKm * c;

        return BigDecimal.valueOf(distanceKm)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDeliveryFee(BigDecimal deliveryDistance) {
        if(deliveryDistance.compareTo(BigDecimal.valueOf(1)) < 0){
            return BigDecimal.ZERO;
        }
        if(deliveryDistance.compareTo(BigDecimal.valueOf(5)) <= 0){
            return BigDecimal.valueOf(10_000);
        }
        if(deliveryDistance.compareTo(BigDecimal.valueOf(10)) <= 0){
            BigDecimal extraKm = deliveryDistance.subtract(BigDecimal.valueOf(5)).setScale(0, RoundingMode.CEILING);
            return BigDecimal.valueOf(10_000).add(extraKm.multiply(BigDecimal.valueOf(3000)));

        }
        if(deliveryDistance.compareTo(BigDecimal.valueOf(20)) <= 0){
            BigDecimal extraKm = deliveryDistance.subtract(BigDecimal.valueOf(10)).setScale(0, RoundingMode.CEILING);
            return BigDecimal.valueOf(25_000).add(extraKm.multiply(BigDecimal.valueOf(5000)));

        }
        throw new IllegalArgumentException(
                "Delivery address is outside the delivery range"
        );

    }
}
